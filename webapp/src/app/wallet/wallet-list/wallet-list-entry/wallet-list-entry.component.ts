import {Component, Input, OnInit} from '@angular/core';
import {DetailedWallet} from "../../_model/detailed-wallet";
import {FormBuilder, FormGroup} from "@angular/forms";
import {PasswordDto} from "../../_model/password-dto";
import {WalletService} from "../../_service/wallet.service";
import {BehaviorSubject} from "rxjs";
import {InstanceStatus, Wallet} from '../../_model/wallet';

@Component({
  selector: 'app-wallet-list-entry',
  templateUrl: './wallet-list-entry.component.html',
  styleUrls: ['./wallet-list-entry.component.scss']
})
export class WalletListEntryComponent implements OnInit {

  private wallet$ = new BehaviorSubject<Wallet>({} as Wallet);

  @Input()
  set wallet(value: Wallet) {
    this.wallet$.next(value);
  };

  get wallet(): Wallet {
    return this.wallet$.getValue();
  }

  public detailedWallet: DetailedWallet;

  public InstanceStatus = InstanceStatus;

  public form: FormGroup;

  public loading = false;

  constructor(
    private walletService: WalletService,
    fb: FormBuilder
  ) {
    this.form = fb.group({
      password: ''
    } as PasswordDto);

    this.wallet$.subscribe(() => {
      this.fetchDetailedWallet();
    })
  }



  private fetchDetailedWallet(): void {
    if (this.wallet.status === InstanceStatus.RUNNING) {
      this.walletService.getDetailedWallet(this.wallet.id).subscribe(walletStatus => {
        this.detailedWallet = walletStatus;
      })
    }
  }

  ngOnInit(): void {

  }

}
