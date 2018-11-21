package iridium

type WalletdRPC interface {
	GetAddresses() (*GetAddressesResponse, error)
}
