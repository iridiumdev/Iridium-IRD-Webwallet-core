package user

import (
	"errors"
	log "github.com/sirupsen/logrus"
	"golang.org/x/crypto/bcrypt"
	"gopkg.in/mgo.v2/bson"
)

type serviceImpl struct {
}

type Service interface {
	CreateUser(user User) (*User, error)
	AuthenticateUser(login Login) (*User, error)
}

var service Service

func (s *serviceImpl) CreateUser(user User) (*User, error) {

	hash, err := bcrypt.GenerateFromPassword([]byte(user.Password), bcrypt.DefaultCost)
	if err != nil {
		log.Errorf("Could not generate password hash for user with username='%s'!", user.Username)
		return nil, err
	}

	user.Password = string(hash)
	user.Id = bson.NewObjectId()

	err = store.InsertUser(&user)
	if err != nil {
		log.Errorf("Could not store user with username='%s'!", user.Username)
		return nil, err
	}

	return &user, nil
}

func (s *serviceImpl) AuthenticateUser(login Login) (*User, error) {

	user, err := store.FindUserByUsername(login.Username)
	if err != nil {
		log.Infof("user with username='%s' not found", login.Username)
		return nil, errors.New("user not found")
	}

	err = bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(login.Password))
	if err != nil {
		log.Warnf("invalid password login attempt for user with username='%s'", login.Username)
		return nil, errors.New("invalid password")
	}

	return user, nil
}

func InitService() Service {
	service = &serviceImpl{}
	return service
}
