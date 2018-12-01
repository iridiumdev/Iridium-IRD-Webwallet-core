package wallet

import (
	"github.com/docker/docker/client"
	"github.com/gin-gonic/gin/json"
	"github.com/iridiumdev/webwallet-core/config"
	"github.com/iridiumdev/webwallet-core/event"
	log "github.com/sirupsen/logrus"
	"sync"
	"time"
)

type watcher struct {
	events       chan *DetailedWallet
	quit         chan struct{}
	dockerClient *client.Client
	eventService event.Service

	running map[string]*LoadedWallet
}

type StatusEvent struct {
	WalletID string
	Status   InstanceStatus
}

type StatusWatcher interface {
	Run() chan *DetailedWallet
	Close()
	AddWallet(wallet *LoadedWallet)
	RemoveWallet(wallet *Wallet)
}

var lock = sync.RWMutex{}
var statusWatcher StatusWatcher

func InitWatcher(dockerClient *client.Client, eventService event.Service) StatusWatcher {
	statusWatcher = &watcher{
		events:       make(chan *DetailedWallet),
		quit:         make(chan struct{}),
		dockerClient: dockerClient,
		eventService: eventService,
		running:      make(map[string]*LoadedWallet),
	}
	return statusWatcher
}

func (w *watcher) AddWallet(wallet *LoadedWallet) {
	lock.Lock()
	defer lock.Unlock()
	w.running[wallet.Id.Hex()] = wallet
}

func (w *watcher) RemoveWallet(wallet *Wallet) {
	lock.Lock()
	defer lock.Unlock()
	delete(w.running, wallet.Id.Hex())
}

func (w *watcher) GetWallets() map[string]*LoadedWallet {
	lock.RLock()
	defer lock.RUnlock()
	return w.running
}

func (w *watcher) Close() {
	close(w.quit)
}

func (w *watcher) Run() chan *DetailedWallet {
	ticker := time.NewTicker(config.Get().Webwallet.Watcher.TickSeconds * time.Second)
	go func() {
		for {
			select {
			case <-w.quit:
				return
			case <-ticker.C:
				w.shutdownOvertimeWallets()
				w.propagateWalletDetails()
			}
		}
	}()

	return w.events
}

// TODO: daniel 29.11.18 - check wallets scheduled for STOP (due to timeout)
func (w *watcher) shutdownOvertimeWallets() {
	lock.Lock()
	defer lock.Unlock()

	for id, wallet := range w.running {
		log.Tracef("Checking instance timeout on wallet %s of user %s", id, wallet.Owner.Hex())
	}
}

func (w *watcher) propagateWalletDetails() {
	wallets := w.GetWallets()

	for id, wallet := range wallets {
		dWallet := &DetailedWallet{}

		dWallet.LoadedWallet = wallet

		rpc, err := service.NewWalletdClient(id)
		if err != nil {
			log.Errorf("Could not create new RPC Client for wallet %s due to: %s", id, err.Error())
			dWallet.Status = ERROR
		}

		dWallet, err = service.FetchDetails(wallet, rpc)
		if err != nil {
			log.Errorf("Could fetch details for wallet %s due to: %s", id, err.Error())
			dWallet.Status = ERROR
		}

		bytes, _ := json.Marshal(dWallet)
		log.Trace(string(bytes))
		w.eventService.SendToUser(dWallet.Owner.Hex(), dWallet)
		//w.events <- dWallet
	}
}
