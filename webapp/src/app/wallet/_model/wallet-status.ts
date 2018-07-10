import {WalletContainer} from "./wallet-container";
import {BlockchainStatus} from "./blockchain-status";

export interface WalletStatus extends WalletContainer {

  currentBlockHeight: number;
  networkBlockHeight: number;

  currentBalance: number;
  lockedBalance: number;

  iridiumStatus: BlockchainStatus;


}
