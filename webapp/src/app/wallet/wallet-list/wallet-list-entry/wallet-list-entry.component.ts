import {Component, Input, OnInit} from '@angular/core';
import {WalletContainer} from "../../_model/wallet-container";
import {WalletContainerStatus} from "../../_model/wallet-container-status";
import {FormBuilder, FormGroup} from "@angular/forms";
import {PasswordBase} from "../../_model/password-base";
import {WalletService} from "../../_service/wallet.service";
import {WalletBase} from "../../_model/wallet-base";
import {WalletStatus} from "../../_model/wallet-status";
import {BehaviorSubject, Observable, Subject} from "rxjs";

@Component({
  selector: 'app-wallet-list-entry',
  templateUrl: './wallet-list-entry.component.html',
  styleUrls: ['./wallet-list-entry.component.scss']
})
export class WalletListEntryComponent implements OnInit {

  private wallet$ = new BehaviorSubject<WalletContainer>({});

  @Input()
  set wallet(value: WalletContainer) {
    this.wallet$.next(value);
  };

  get wallet(): WalletContainer {
    return this.wallet$.getValue();
  }

  public walletStatus: WalletStatus;

  public ContainerStatus = WalletContainerStatus;

  public form: FormGroup;

  public loading = false;

  constructor(
    private walletService: WalletService,
    fb: FormBuilder
  ) {
    this.form = fb.group({
      password: ''
    } as PasswordBase);

    this.wallet$.subscribe(wallet => {
      this.refreshWalletStatus();
    })
  }

  loadWallet() {
    const walletBase: WalletBase = this.wallet;
    walletBase.password = (this.form.value as PasswordBase).password;

    this.loading = true;
    this.walletService.loadWallet(walletBase).subscribe(wallet => {
      this.wallet = wallet;
    }, error => {
      console.error("Error!");
      console.error(error);
    }, () => {
      this.loading = false;
    })

  }

  private refreshWalletStatus(): void {
    if (this.wallet.containerStatus === WalletContainerStatus.RUNNING) {
      this.walletService.getWalletStatus(this.wallet.address).subscribe(walletStatus => {
        this.walletStatus = walletStatus;
      })
    }
  }

  ngOnInit(): void {

  }

}
