package wallet

import (
	"context"
	"fmt"
	"github.com/docker/docker/api/types"
	"github.com/docker/docker/api/types/container"
	"github.com/docker/docker/api/types/volume"
	"github.com/docker/docker/client"
	"github.com/iridiumdev/webwallet-core/config"
	"github.com/pkg/errors"
	log "github.com/sirupsen/logrus"
	"github.com/ybbus/jsonrpc"
	"gopkg.in/mgo.v2/bson"
	"time"
)

type serviceImpl struct {
	dockerClient *client.Client
}

type Service interface {
	CreateWallet(dto CreateDTO, userId string) (*Wallet, error)
	ImportWallet(dto ImportDTO, userId string) (*Wallet, error)
	GetWallets(userId string) ([]*Wallet, error)
}

var service Service

// TODO: daniel 08.11.18 - implement
func (s *serviceImpl) CreateWallet(dto CreateDTO, userId string) (*Wallet, error) {

	wallet := &Wallet{
		Id:    bson.NewObjectId(),
		Name:  dto.Name,
		Owner: userId,
	}

	ctx := context.Background()

	log.Infof("Creating new volume for wallet with id '%s'", wallet.Id.Hex())

	vol, err := s.dockerClient.VolumeCreate(ctx, volume.VolumesCreateBody{
		Name: fmt.Sprintf("%s.wallet", wallet.Id.Hex()),
	})

	command := append(config.Get().Webwallet.Satellite.Command,
		fmt.Sprintf("--container-password=%s", dto.Password),
	)

	log.Infof("Creating new container for wallet with id '%s'", wallet.Id.Hex())

	resp, err := s.dockerClient.ContainerCreate(ctx, &container.Config{
		Image: config.Get().Webwallet.Satellite.Image,
		Cmd:   command,
		Volumes: map[string]struct{}{
			fmt.Sprintf("%s:/data", vol.Name): {},
		},
	}, nil, nil, wallet.Id.Hex())

	if err != nil {
		return nil, err
	}

	log.Infof("Attaching network '%s' to container for wallet with id '%s'", config.Get().Webwallet.Network, wallet.Id.Hex())

	if err := s.dockerClient.NetworkConnect(ctx, config.Get().Webwallet.Network, wallet.Id.Hex(), nil); err != nil {
		return nil, err
	}

	log.Infof("Starting container for wallet with id '%s'", wallet.Id.Hex())

	if err := s.dockerClient.ContainerStart(ctx, resp.ID, types.ContainerStartOptions{}); err != nil {
		return nil, err
	}

	log.Infof("Started container for wallet with id '%s'", wallet.Id.Hex())

	time.Sleep(1 * time.Second) // TODO: daniel 11.11.18 - please. no.

	rpcClient, err := s.buildRpcClient(wallet.Id.Hex())
	if err != nil {
		return nil, err
	}

	response, err := rpcClient.Call("getAddresses")
	if err != nil {
		return nil, err
	}

	result := struct {
		Addresses []string `json:"addresses"`
	}{}
	response.GetObject(&result)

	if len(result.Addresses) > 0 {
		wallet.Address = result.Addresses[0]
	} else {
		return nil, errors.New("could not fetch wallet address!")
	}

	err = store.InsertWallet(wallet)

	return wallet, err
}

func (s *serviceImpl) buildRpcClient(walletId string) (jsonrpc.RPCClient, error) {
	containerEndpoint, err := s.resolveContainerEndpoint(walletId)
	if err != nil {
		return nil, err
	}
	rpcHost := fmt.Sprintf("%s:%s", containerEndpoint, config.Get().Webwallet.Satellite.RpcPort)
	rpcAddress := fmt.Sprintf("http://%s/json_rpc", rpcHost)
	log.Debugf("Connecting to %s wallets rpc client at: %s", walletId, rpcAddress)

	//conn, err := net.DialTimeout("tcp", rpcHost, 10 * time.Second)
	//if conn != nil {
	//	conn.Close()
	//}
	//if err != nil {
	//	return nil, err
	//}
	return jsonrpc.NewClient(rpcAddress), nil
}

// TODO: daniel 08.11.18 - implement
func (s *serviceImpl) ImportWallet(dto ImportDTO, userId string) (*Wallet, error) {
	// TODO: daniel 08.11.18 - create docker volume
	// TODO: daniel 08.11.18 - create docker container
	// TODO: daniel 08.11.18 - create walletd file
	// TODO: daniel 08.11.18 - import wallet from keys in walletd
	// TODO: daniel 08.11.18 - fetch address
	// TODO: daniel 08.11.18 - build Wallet struct
	// TODO: daniel 08.11.18 - save to db
	return nil, nil
}

func (s *serviceImpl) GetWallets(userId string) ([]*Wallet, error) {
	return store.FindWalletsByOwner(userId)
}

func InitService(dockerClient *client.Client) Service {
	service = &serviceImpl{dockerClient: dockerClient}
	return service
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
