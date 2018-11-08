package wallet

import "gopkg.in/mgo.v2/bson"

type CreateDTO struct {
	Password string
	Name     string
}

type ImportDTO struct {
	CreateDTO
	ViewSecretKey  string
	SpendSecretKey string
}

type Wallet struct {
	Id   bson.ObjectId `json:"id" bson:"_id,omitempty"`
	Name string        `json:"name" bson:"name,omitempty"`
}
