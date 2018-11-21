package iridium

import (
	"errors"
	log "github.com/sirupsen/logrus"
	"github.com/ybbus/jsonrpc"
	"net"
	"net/url"
	"time"
)

type WalletdRPC interface {
	Reset(viewSecretKey string) error
	CreateAddress(spendSecretKey string) (string, error)
	GetAddresses() ([]string, error)
}

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

func (c *client) GetAddresses() ([]string, error) {
	var response *jsonrpc.RPCResponse
	var err error

	response, err = c.rpc.Call("getAddresses")
	if err != nil {
		return nil, err
	}

	result := &GetAddressesResponse{}
	err = response.GetObject(&result)
	if err != nil {
		err = response.Error
	}

	return result.Addresses, err
}
func (c *client) CreateAddress(spendSecretKey string) (string, error) {
	var response *jsonrpc.RPCResponse
	var err error

	params := struct {
		SpendSecretKey string `json:"spendSecretKey"`
	}{SpendSecretKey: spendSecretKey}
	response, err = c.rpc.Call("createAddress", params)
	if err != nil {
		return "", err
	}

	result := &CreateAddressResponse{}
	err = response.GetObject(&result)
	if err != nil {
		err = response.Error
	}

	return result.Address, err
}

func (c *client) Reset(viewSecretKey string) error {
	var response *jsonrpc.RPCResponse
	var err error

	if viewSecretKey != "" {
		params := struct {
			ViewSecretKey string `json:"viewSecretKey"`
		}{ViewSecretKey: viewSecretKey}
		response, err = c.rpc.Call("reset", params)
	} else {
		response, err = c.rpc.Call("reset")
	}

	err = handleRPCError(response)

	return err
}

func handleRPCError(response *jsonrpc.RPCResponse) error {
	if response.Error != nil {
		return response.Error
	}
	return nil
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
