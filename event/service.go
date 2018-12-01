package event

import (
	"encoding/json"
	"github.com/iridiumdev/webwallet-core/event/ws"
	log "github.com/sirupsen/logrus"
)

type Service interface {
	WSHub() *ws.Hub
	SendToUser(userId string, message interface{})
}

var (
//ErrCouldNotStartWallet = errors.New("wallet could not be started")
)

var service Service

type serviceImpl struct {
	hub *ws.Hub
}

func InitService() Service {
	service = &serviceImpl{hub: ws.NewHub()}
	return service
}

func (s *serviceImpl) WSHub() *ws.Hub {
	return s.hub
}

func (s *serviceImpl) SendToUser(userId string, message interface{}) {

	bytes, err := json.Marshal(message)
	if err != nil {
		log.Errorf("Could not convert message %s for user %s to []byte!", message, userId)
		return
	}

	s.hub.SendToUser(userId, bytes)
}
