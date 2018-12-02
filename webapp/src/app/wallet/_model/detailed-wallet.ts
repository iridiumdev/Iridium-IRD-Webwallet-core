import {Wallet} from "./wallet";


export interface BlockHeight {
  current: number;
  top: number;
}

export interface Balance {
  total: number;
  locked: number;
}

export interface DetailedWallet extends Wallet{

  balance: Balance;
  blockHeight: BlockHeight;
  peerCount: number;
}
