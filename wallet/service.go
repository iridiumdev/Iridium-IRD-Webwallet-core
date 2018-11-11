package wallet

import (
	"context"
	"fmt"
	"github.com/docker/docker/api/types"
	"github.com/docker/docker/api/types/container"
	"github.com/docker/docker/api/types/volume"
	"github.com/docker/docker/client"
	"github.com/iridiumdev/webwallet-core/config"
	log "github.com/sirupsen/logrus"
	"gopkg.in/mgo.v2/bson"
)

type serviceImpl struct {
	dockerClient *client.Client
}

type Service interface {
	CreateWallet(dto CreateDTO) (*Wallet, error)
	ImportWallet(dto ImportDTO) (*Wallet, error)
}

var service Service

// TODO: daniel 08.11.18 - implement
func (s *serviceImpl) CreateWallet(dto CreateDTO) (*Wallet, error) {
	// TODO: daniel 08.11.18 - fetch address
	// TODO: daniel 08.11.18 - build Wallet struct

	wallet := &Wallet{
		Id:   bson.NewObjectId(),
		Name: dto.Name,
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

	err = store.InsertWallet(wallet)

	return wallet, err
}

// TODO: daniel 08.11.18 - implement
func (s *serviceImpl) ImportWallet(dto ImportDTO) (*Wallet, error) {
	// TODO: daniel 08.11.18 - create docker volume
	// TODO: daniel 08.11.18 - create docker container
	// TODO: daniel 08.11.18 - create walletd file
	// TODO: daniel 08.11.18 - import wallet from keys in walletd
	// TODO: daniel 08.11.18 - fetch address
	// TODO: daniel 08.11.18 - build Wallet struct
	// TODO: daniel 08.11.18 - save to db
	return nil, nil
}

func InitService(dockerClient *client.Client) {
	service = &serviceImpl{dockerClient: dockerClient}
}
