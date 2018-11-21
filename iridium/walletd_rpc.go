package iridium

import (
	"errors"
	log "github.com/sirupsen/logrus"
	"github.com/ybbus/jsonrpc"
	"net"
	"net/url"
	"time"
)

type client struct {
	address *url.URL
	rpc     jsonrpc.RPCClient
}

func Walletd(address string) (WalletdRPC, error) {

	parsedAddress, err := url.Parse(address)
	if err != nil {
		return nil, err
	}

	rpcClient, err := buildRpcClient(parsedAddress)
	if err != nil {
		return nil, err
	}

	return &client{
		address: parsedAddress,
		rpc:     rpcClient,
	}, nil
}

func (c *client) GetAddresses() (*GetAddressesResponse, error) {
	response, err := c.rpc.Call("getAddresses")
	if err != nil {
		return nil, err
	}

	result := &GetAddressesResponse{}
	err = response.GetObject(&result)

	return result, err
}

func buildRpcClient(address *url.URL) (jsonrpc.RPCClient, error) {

	tcpSocket := address.Host
	log.Debugf("Connecting to walletd RPC at: %s", address)

	startTime := time.Now()

	signal := make(chan bool)
	quit := make(chan bool)
	go func() {
		for {
			select {
			case <-quit:
				return
			default:
				conn, _ := net.DialTimeout("tcp", tcpSocket, 100*time.Millisecond)
				if conn != nil {
					conn.Close()
					signal <- true
					return
				}
			}

		}

	}()

	timeout := time.Duration(5) * time.Second
	log.Debugf("Waiting (timeout: %s) for connection to walletd RPC at: %s", timeout, address)
	select {
	case <-signal:
		elapsedTime := time.Since(startTime)
		log.Debugf("RPC Connection to walletd succeeded after %s at: %s", elapsedTime, tcpSocket)
	case <-time.After(timeout):
		quit <- true
		log.Errorf("RPC Connection to walletd timed out after %s at: %s", timeout, tcpSocket)
		return nil, errors.New("rpc connection timeout")
	}

	return jsonrpc.NewClient(address.String()), nil
}
