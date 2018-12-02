import {Injectable} from '@angular/core';
import {WalletModule} from "../wallet.module";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {DetailedWallet} from "../_model/detailed-wallet";
import {PasswordDto} from "../_model/password-dto";
import {Wallet} from "../_model/wallet";

@Injectable({
  providedIn: WalletModule
})
export class WalletService {

  constructor(private http: HttpClient) {
  }

  getWalletList(): Observable<Wallet[]> {
    return this.http.get<Wallet[]>('/api/v1/wallets')
  }

  loadWallet(id: string, pw: PasswordDto): Observable<DetailedWallet> {
    return this.http.post<DetailedWallet>(`/api/v1/wallets/${id}/instance`, pw)
  }

  getDetailedWallet(id: string): Observable<DetailedWallet> {
    return this.http.get<DetailedWallet>(`/api/v1/wallets/${id}`)
  }

}
