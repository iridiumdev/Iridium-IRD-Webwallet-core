package ws

import (
	log "github.com/sirupsen/logrus"
	"sync"
	"time"
)

type Hub struct {
	// the mutex to protect clients
	connectionsMx sync.RWMutex

	// Registered clients.
	clients map[string]map[*Connection]struct{}

	// Inbound messages from the clients.
	inbound chan []byte
}

func NewHub() *Hub {
	h := &Hub{
		connectionsMx: sync.RWMutex{},
		inbound:       make(chan []byte),
		clients:       make(map[string]map[*Connection]struct{}),
	}

	// TODO: daniel 01.12.18 - decide if we actually need to reed from ws
	// TODO: daniel 01.12.18 - remove that broadcast to all client->connections stuff
	go func() {
		for {
			msg := <-h.inbound
			h.connectionsMx.RLock()
			for userId, connections := range h.clients {

				for c := range connections {
					select {
					case c.send <- msg:
						// stop trying to send to this connection after trying for 1 second.
						// if we have to stop, it means that a reader died so remove the connection also.
					case <-time.After(1 * time.Second):
						log.Printf("shutting down connection %v", c)
						h.CloseConnection(userId, c)
					}
				}

			}
			h.connectionsMx.RUnlock()
		}
	}()
	return h
}

func (h *Hub) AddConnection(userId string, conn *Connection) *sync.WaitGroup {
	h.connectionsMx.Lock()
	defer h.connectionsMx.Unlock()

	_, ok := h.clients[userId]
	if !ok {
		h.clients[userId] = make(map[*Connection]struct{})
	}

	h.clients[userId][conn] = struct{}{}

	conn.h = h
	conn.userId = userId

	wg := &sync.WaitGroup{}
	wg.Add(2)

	conn.wg = wg

	return wg
}

func (h *Hub) CloseConnection(userId string, conn *Connection) {
	h.connectionsMx.Lock()
	defer h.connectionsMx.Unlock()
	if connections, ok := h.clients[userId]; ok {

		if _, ok := connections[conn]; ok {
			defer conn.wsConn.Close()
			delete(connections, conn)
			close(conn.send)
		}

	}
}

func (h *Hub) SendToUser(userId string, message []byte) {
	go func() {
		h.connectionsMx.RLock()
		defer h.connectionsMx.RUnlock()

		if connections, ok := h.clients[userId]; ok {

			for c := range connections {
				select {
				case c.send <- message:
					// stop trying to send to this connection after trying for 1 second.
					// if we have to stop, it means that a reader died so remove the connection also.
				case <-time.After(1 * time.Second):
					log.Printf("shutting down connection %v", c)
					h.CloseConnection(userId, c)
				}
			}

		}

	}()

}
