package wallet

import (
	"gopkg.in/mgo.v2"
	"gopkg.in/mgo.v2/bson"
)

type mongoDb struct {
	db      *mgo.Database
	wallets *mgo.Collection
}

type Store interface {
	InsertWallet(wallet *Wallet) error
	FindWalletsByOwner(userId bson.ObjectId) ([]*Wallet, error)
	FindWalletByOwner(walletId bson.ObjectId, userId bson.ObjectId) (*Wallet, error)
}

var store Store

func (db *mongoDb) InsertWallet(wallet *Wallet) error {
	err := db.wallets.Insert(wallet)
	return err
}

func (db *mongoDb) FindWalletsByOwner(userId bson.ObjectId) ([]*Wallet, error) {
	var results []*Wallet
	err := db.wallets.Find(bson.M{"owner": userId}).All(&results)
	return results, err
}

func (db *mongoDb) FindWalletByOwner(walletId bson.ObjectId, userId bson.ObjectId) (*Wallet, error) {
	var result *Wallet
	err := db.wallets.Find(bson.M{"_id": walletId, "owner": userId}).One(&result)
	return result, err
}

func InitStore(db *mgo.Database) {
	store = &mongoDb{db: db, wallets: db.C("wallets")}
}
