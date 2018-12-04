import {PasswordDto} from "./password-dto";

export interface CreateDto extends PasswordDto{
  name: string;
}
