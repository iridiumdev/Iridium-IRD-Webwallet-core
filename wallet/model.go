package wallet

import "gopkg.in/mgo.v2/bson"

type PasswordDTO struct {
	Password string `json:"password" binding:"required,min=8"`
}

type CreateDTO struct {
	PasswordDTO
	Name string `json:"name" binding:"required,max=255"`
}

type ImportDTO struct {
	CreateDTO
	ViewSecretKey  string `json:"viewSecretKey"`
	SpendSecretKey string `json:"spendSecretKey"`
}

type InstanceStatus string

const (
	STOPPED InstanceStatus = "STOPPED"
	RUNNING InstanceStatus = "RUNNING"
	ERROR   InstanceStatus = "ERROR"
)

type Balance struct {
	Total  uint64 `json:"total"`
	Locked uint64 `json:"locked"`
}

type BlockHeight struct {
	Current uint32 `json:"current"`
	Top     uint32 `json:"top"`
}

type Wallet struct {
	Id      bson.ObjectId  `json:"id" bson:"_id,omitempty"`
	Name    string         `json:"name" bson:"name"`
	Address string         `json:"address" bson:"address"`
	Owner   bson.ObjectId  `json:"owner" bson:"owner"`
	Status  InstanceStatus `json:"status"`
}

type LoadedWallet struct {
	*Wallet
}

type DetailedWallet struct {
	*LoadedWallet
	Balance     Balance     `json:"balance"`
	BlockHeight BlockHeight `json:"blockHeight"`
	PeerCount   uint8       `json:"peerCount"`
}
