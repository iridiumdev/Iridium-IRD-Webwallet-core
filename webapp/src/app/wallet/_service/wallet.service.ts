import {Injectable} from '@angular/core';
import {WalletModule} from "../wallet.module";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {WalletContainer} from "../_model/wallet-container";
import {WalletBase} from "../_model/wallet-base";
import {WalletStatus} from "../_model/wallet-status";

@Injectable({
  providedIn: WalletModule
})
export class WalletService {

  constructor(private http: HttpClient) {
  }

  getWalletList(): Observable<WalletContainer[]> {
    return this.http.get<WalletContainer[]>('/api/wallets')
  }

  loadWallet(walletBase: WalletBase): Observable<WalletContainer> {
    return this.http.post<WalletContainer>(`/api/wallets/${walletBase.address}`, walletBase)
  }

  getWalletStatus(address: string): Observable<WalletStatus> {
    return this.http.get<WalletStatus>(`/api/wallets/${address}`)
  }

}
