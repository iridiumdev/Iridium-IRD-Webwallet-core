package iridium

type GetAddressesResponse struct {
	Addresses []string `json:"addresses"`
}

type CreateAddressResponse struct {
	Address string `json:"address"`
}
