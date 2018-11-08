package wallet

import "gopkg.in/mgo.v2/bson"

type serviceImpl struct {
}

type Service interface {
	CreateWallet(dto CreateDTO) (*Wallet, error)
	ImportWallet(dto ImportDTO) (*Wallet, error)
}

// TODO: daniel 08.11.18 - initialize this somehow better
var service Service = &serviceImpl{}

// TODO: daniel 08.11.18 - implement
func (s *serviceImpl) CreateWallet(dto CreateDTO) (*Wallet, error) {
	// TODO: daniel 08.11.18 - create docker volume
	// TODO: daniel 08.11.18 - create docker container
	// TODO: daniel 08.11.18 - create walletd file
	// TODO: daniel 08.11.18 - fetch address
	// TODO: daniel 08.11.18 - build Wallet struct

	wallet := &Wallet{
		Id:   bson.NewObjectId(),
		Name: dto.Name,
	}

	err := store.InsertWallet(wallet)

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
