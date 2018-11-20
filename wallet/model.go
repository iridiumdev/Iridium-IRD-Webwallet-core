package wallet

import "gopkg.in/mgo.v2/bson"

type CreateDTO struct {
	Password string `json:"password" binding:"required,min=8"`
	Name     string `json:"name" binding:"required,max=255"`
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
	Owner   string        `json:"owner" bson:"owner"`
}
