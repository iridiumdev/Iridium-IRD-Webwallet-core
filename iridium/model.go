package iridium

type GetAddressesResponse struct {
	Addresses []string `json:"addresses"`
}

type CreateAddressResponse struct {
	Address string `json:"address"`
}

type GetStatusResponse struct {
	BlockCount      uint32 `json:"blockCount"`
	KnownBlockCount uint32 `json:"knownBlockCount"`
	PeerCount       uint8  `json:"peerCount"`
	LastBlockHash   string `json:"lastBlockHash"`
}

type GetBalanceResponse struct {
	AvailableBalance uint64 `json:"availableBalance"`
	LockedAmount     uint64 `json:"lockedAmount"`
}
