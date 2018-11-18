package wallet

import "gopkg.in/mgo.v2/bson"

type CreateDTO struct {
	Password string `json:"password"`
	Name     string `json:"name"`
}

type ImportDTO struct {
	CreateDTO
	ViewSecretKey  string `json:"viewSecretKey"`
	SpendSecretKey string `json:"spendSecretKey"`
}

type Wallet struct {
	Id      bson.ObjectId `json:"id" bson:"_id,omitempty"`
	Name    string        `json:"name" bson:"name"`
	Address string        `json:"address" bson:"address"`
}
