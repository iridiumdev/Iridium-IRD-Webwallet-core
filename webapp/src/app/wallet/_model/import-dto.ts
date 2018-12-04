import {CreateDto} from "./create-dto";

export interface ImportDto extends CreateDto{
  viewSecretKey: string;
  spendSecretKey: string;
}
