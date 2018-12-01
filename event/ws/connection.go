package ws

import (
	"github.com/gorilla/websocket"
	log "github.com/sirupsen/logrus"
	"sync"
)

type Connection struct {
	// Buffered channel of outbound messages.
	send chan []byte

	// The Hub.
	h *Hub

	wsConn *websocket.Conn

	wg *sync.WaitGroup

	userId string
}

type AuthenticatedConnection struct {
	*Connection
}

func NewConnection(wsConn *websocket.Conn) *Connection {
	return &Connection{
		send:   make(chan []byte, 256),
		wsConn: wsConn,
	}
}

// TODO: daniel 01.12.18 - decide if we actually need to reed from ws
func (c *Connection) Reader() {
	defer c.wg.Done()
	for {
		_, message, err := c.wsConn.ReadMessage()
		log.Tracef("<<< websocket reader: %s", string(message))
		if err != nil {
			break
		}

		c.h.inbound <- message

	}
}

func (c *Connection) Writer() {
	defer c.wg.Done()
	for message := range c.send {
		err := c.wsConn.WriteMessage(websocket.TextMessage, message)
		log.Tracef(">>> websocket writer: %s", string(message))
		if err != nil {
			break
		}
	}
}
