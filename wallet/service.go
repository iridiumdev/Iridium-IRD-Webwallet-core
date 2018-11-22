package wallet

import (
	"context"
	"fmt"
	"github.com/docker/docker/api/types"
	"github.com/docker/docker/api/types/container"
	"github.com/docker/docker/api/types/filters"
	"github.com/docker/docker/api/types/mount"
	"github.com/docker/docker/api/types/volume"
	"github.com/docker/docker/client"
	"github.com/iridiumdev/webwallet-core/config"
	"github.com/iridiumdev/webwallet-core/iridium"
	"github.com/pkg/errors"
	log "github.com/sirupsen/logrus"
	"gopkg.in/mgo.v2/bson"
	"net"
)

type Service interface {
	CreateWallet(dto CreateDTO, userId string) (*DetailedWallet, error)
	ImportWallet(dto ImportDTO, userId string) (*DetailedWallet, error)

	GetWallets(userId string) ([]*Wallet, error)
	GetWallet(walletId string, userId string) (*DetailedWallet, error)

	StartWallet(walletId string, password string, userId string) (*DetailedWallet, error)
	StopWallet(walletId string, userId string) (*Wallet, error)
}

var (
	ErrWalletNotFound   = errors.New("wallet not found")
	ErrWalletNotRunning = errors.New("wallet not running")
)

var service Service

type serviceImpl struct {
	dockerClient *client.Client
}

func InitService(dockerClient *client.Client) Service {
	service = &serviceImpl{dockerClient: dockerClient}
	return service
}

func (s *serviceImpl) CreateWallet(dto CreateDTO, userId string) (*DetailedWallet, error) {

	wallet := &Wallet{
		Id:    bson.NewObjectId(),
		Name:  dto.Name,
		Owner: bson.ObjectIdHex(userId),
	}

	if err := s.createNewVolume(wallet); err != nil {
		return nil, err
	}
	if _, err := s.instantiateContainer(wallet, dto.Password); err != nil {
		return nil, err
	}

	walletd, err := s.newWalletdClient(wallet.Id.Hex())
	if err != nil {
		return nil, err
	}

	addresses, err := walletd.GetAddresses()
	if err != nil {
		return nil, err
	}

	if len(addresses) > 0 {
		wallet.Address = addresses[0]
	} else {
		return nil, errors.New("could not fetch wallet address!")
	}

	err = store.InsertWallet(wallet)

	lWallet := &LoadedWallet{Wallet: wallet}

	dWallet, err := s.fetchDetails(lWallet, walletd)

	return dWallet, err
}

func (s *serviceImpl) ImportWallet(dto ImportDTO, userId string) (*DetailedWallet, error) {

	wallet := &Wallet{
		Id:    bson.NewObjectId(),
		Name:  dto.Name,
		Owner: bson.ObjectIdHex(userId),
	}

	if err := s.createNewVolume(wallet); err != nil {
		return nil, err
	}
	if _, err := s.instantiateContainer(wallet, dto.Password); err != nil {
		return nil, err
	}

	walletd, err := s.newWalletdClient(wallet.Id.Hex())
	if err != nil {
		return nil, err
	}

	if err := walletd.Reset(dto.ViewSecretKey); err != nil {
		return nil, err
	}
	address, err := walletd.CreateAddress(dto.SpendSecretKey)
	if err != nil {
		return nil, err
	}

	walletd.Save()
	if err != nil {
		return nil, err
	}

	wallet.Address = address
	err = store.InsertWallet(wallet)

	lWallet := &LoadedWallet{Wallet: wallet}

	dWallet, err := s.fetchDetails(lWallet, walletd)

	return dWallet, err
}

func (s *serviceImpl) GetWallets(userId string) ([]*Wallet, error) {
	return store.FindWalletsByOwner(bson.ObjectIdHex(userId))
}

func (s *serviceImpl) GetWallet(walletId string, userId string) (*DetailedWallet, error) {

	wallet, err := store.FindWalletByOwner(bson.ObjectIdHex(walletId), bson.ObjectIdHex(userId))
	if err != nil || wallet == nil {
		log.Errorf("Could not find wallet %s for user %s, err: %s", walletId, userId, err.Error())
		return nil, ErrWalletNotFound
	}

	lWallet := &LoadedWallet{Wallet: wallet}
	dWallet := &DetailedWallet{LoadedWallet: lWallet}

	ctx := context.Background()

	listFilters := filters.NewArgs()
	listFilters.Add("name", wallet.Id.Hex())
	listFilters.Add("status", "running")

	cList, err := s.dockerClient.ContainerList(ctx, types.ContainerListOptions{
		Limit:   1,
		Filters: listFilters,
	})
	if err != nil {
		return nil, err
	}

	if len(cList) == 0 {
		return dWallet, ErrWalletNotRunning
	}

	walletd, err := s.newWalletdClient(wallet.Id.Hex())
	if err != nil {
		dWallet.Status = ERROR
		return dWallet, err
	}

	dWallet, err = s.fetchDetails(lWallet, walletd)

	return dWallet, err
}

func (s *serviceImpl) StartWallet(walletId string, password string, userId string) (*DetailedWallet, error) {

	wallet, err := store.FindWalletByOwner(bson.ObjectIdHex(walletId), bson.ObjectIdHex(userId))
	if err != nil || wallet == nil {
		log.Errorf("Could not find wallet %s for user %s, err: %s", walletId, userId, err.Error())
		return nil, ErrWalletNotFound
	}

	loadedWallet, err := s.instantiateContainer(wallet, password)
	if err != nil {
		return nil, err
	}

	walletd, err := s.newWalletdClient(wallet.Id.Hex())
	if err != nil {
		return nil, err
	}

	detailedWallet, err := s.fetchDetails(loadedWallet, walletd)

	return detailedWallet, err
}

// TODO: daniel 22.11.18 - implement!
func (s *serviceImpl) StopWallet(walletId string, userId string) (*Wallet, error) {
	return nil, nil
}

func (s *serviceImpl) createNewVolume(wallet *Wallet) error {
	ctx := context.Background()

	log.Infof("Creating new volume for wallet with id '%s'", wallet.Id.Hex())
	_, err := s.dockerClient.VolumeCreate(ctx, volume.VolumesCreateBody{
		Name: fmt.Sprintf("%s.wallet", wallet.Id.Hex()),
	})
	if err != nil {
		return err
	}
	log.Debugf("Created new volume for wallet with id '%s' successfully!", wallet.Id.Hex())
	return nil
}

func (s *serviceImpl) instantiateContainer(wallet *Wallet, password string) (*LoadedWallet, error) {
	ctx := context.Background()

	command := append(config.Get().Webwallet.Satellite.Command,
		fmt.Sprintf("--container-password=%s", password),
	)

	volumeName := fmt.Sprintf("%s.wallet", wallet.Id.Hex())
	_, err := s.dockerClient.ContainerCreate(ctx, &container.Config{
		Image: config.Get().Webwallet.Satellite.Image,
		Cmd:   command,
	}, &container.HostConfig{
		Mounts: []mount.Mount{
			{
				Type:   mount.TypeVolume,
				Source: volumeName,
				Target: "/data",
			},
		},
	}, nil, wallet.Id.Hex())

	if err != nil {
		return nil, err
	}

	log.Infof("Attaching network '%s' to container for wallet with id '%s'", config.Get().Webwallet.Network, wallet.Id.Hex())

	if err := s.dockerClient.NetworkConnect(ctx, config.Get().Webwallet.Network, wallet.Id.Hex(), nil); err != nil {
		return nil, err
	}

	log.Infof("Starting container for wallet with id '%s'", wallet.Id.Hex())

	if err := s.dockerClient.ContainerStart(ctx, wallet.Id.Hex(), types.ContainerStartOptions{}); err != nil {
		return nil, err
	}

	log.Debugf("Started container for wallet with id '%s'", wallet.Id.Hex())

	loadedWallet := &LoadedWallet{
		Wallet: wallet,
	}

	return loadedWallet, err
}

func (s *serviceImpl) fetchDetails(wallet *LoadedWallet, rpc iridium.WalletdRPC) (*DetailedWallet, error) {
	dWallet := &DetailedWallet{LoadedWallet: wallet}
	dWallet.Status = RUNNING

	sRes, err := rpc.GetStatus()
	if err != nil {
		wallet.Status = ERROR
		return dWallet, err
	}

	dWallet.BlockHeight = BlockHeight{
		Current: sRes.BlockCount,
		Top:     sRes.KnownBlockCount,
	}
	dWallet.PeerCount = sRes.PeerCount

	bRes, err := rpc.GetBalance()
	if err != nil {
		wallet.Status = ERROR
		return dWallet, err
	}

	dWallet.Balance = Balance{
		Total:  bRes.AvailableBalance,
		Locked: bRes.LockedAmount,
	}

	return dWallet, nil
}

func (s *serviceImpl) newWalletdClient(walletId string) (iridium.WalletdRPC, error) {
	containerEndpoint, err := s.resolveContainerEndpoint(walletId)
	if err != nil {
		return nil, err
	}
	rpcHost := net.JoinHostPort(containerEndpoint, config.Get().Webwallet.Satellite.RpcPort)
	rpcAddress := fmt.Sprintf("http://%s/json_rpc", rpcHost)

	return iridium.Walletd(rpcAddress)
}

func (s *serviceImpl) resolveContainerEndpoint(containerId string) (string, error) {
	ctx := context.Background()

	if config.Get().Webwallet.InternalResolver {
		return containerId, nil
	} else {
		log.Debugf("Using 'ip' resolver to get the satellites endpoint address")
		inspect, err := s.dockerClient.ContainerInspect(ctx, containerId)
		if err != nil {
			return "", err
		}

		return inspect.NetworkSettings.Networks[config.Get().Webwallet.Network].IPAddress, nil
	}

}
