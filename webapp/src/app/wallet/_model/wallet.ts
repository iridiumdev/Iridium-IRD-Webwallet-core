import {PasswordDto} from "./password-dto";

export enum InstanceStatus {
  STOPPED = "STOPPED",
  RUNNING = "RUNNING",
  ERROR = "ERROR"
}

export interface Wallet extends PasswordDto {

  id: string;
  name: string;
  address: string;
  owner: string;
  status: InstanceStatus;

}
