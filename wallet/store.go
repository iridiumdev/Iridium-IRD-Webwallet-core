package wallet

import (
	"gopkg.in/mgo.v2"
)

type mongoDb struct {
	db      *mgo.Database
	wallets *mgo.Collection
}

type Store interface {
	InsertWallet(wallet *Wallet) error
	FindWallets() ([]*Wallet, error)
}

var store Store

func (db *mongoDb) InsertWallet(wallet *Wallet) error {
	err := db.wallets.Insert(wallet)
	return err
}

func (db *mongoDb) FindWallets() ([]*Wallet, error) {
	var results []*Wallet
	err := db.wallets.Find(nil).All(&results)
	return results, err
}

func InitStore(db *mgo.Database) {
	store = &mongoDb{db: db, wallets: db.C("wallets")}
}
