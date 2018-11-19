package user

import (
	"gopkg.in/mgo.v2"
	"gopkg.in/mgo.v2/bson"
)

type mongoDb struct {
	db    *mgo.Database
	users *mgo.Collection
}

type Store interface {
	InsertUser(user *User) error
	FindUserByUsername(username string) (*User, error)
}

var store Store

func (db *mongoDb) InsertUser(user *User) error {
	err := db.users.Insert(user)
	return err
}

func (db *mongoDb) FindUserByUsername(username string) (*User, error) {
	var result *User
	err := db.users.Find(bson.M{"username": username}).One(&result)
	return result, err
}

func InitStore(db *mgo.Database) {
	usersCollection := db.C("users")
	usersCollection.EnsureIndex(mgo.Index{Key: []string{"username"}, Unique: true})
	store = &mongoDb{db: db, users: usersCollection}
}
