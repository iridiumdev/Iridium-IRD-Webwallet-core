import {Injectable} from '@angular/core';
import {WalletModule} from "../wallet.module";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {DetailedWallet} from "../_model/detailed-wallet";
import {PasswordDto} from "../_model/password-dto";
import {Wallet} from "../_model/wallet";
import {ImportDto} from "../_model/import-dto";
import {CreateDto} from "../_model/create-dto";

@Injectable({
  providedIn: WalletModule
})
export class WalletService {

  constructor(private http: HttpClient) {
  }

  createWallet(dto: CreateDto): Observable<DetailedWallet> {
    return this.http.post<DetailedWallet>('/api/v1/wallets', dto)
  }

  importWallet(dto: ImportDto): Observable<DetailedWallet> {
    return this.http.post<DetailedWallet>('/api/v1/wallets', dto)
  }

  getWalletList(): Observable<Wallet[]> {
    return this.http.get<Wallet[]>('/api/v1/wallets')
  }

  loadWallet(id: string, pw: PasswordDto): Observable<DetailedWallet> {
    return this.http.post<DetailedWallet>(`/api/v1/wallets/${id}/instance`, pw)
  }

  lockWallet(id: string): Observable<Wallet> {
    return this.http.delete<Wallet>(`/api/v1/wallets/${id}/instance`)
  }

  getDetailedWallet(id: string): Observable<DetailedWallet> {
    return this.http.get<DetailedWallet>(`/api/v1/wallets/${id}`)
  }

}
