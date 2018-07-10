import {WalletContainerStatus} from "./wallet-container-status";
import {WalletBase} from "./wallet-base";

export interface WalletContainer extends WalletBase{

  containerStatus: WalletContainerStatus;

  containerName: string;
}
