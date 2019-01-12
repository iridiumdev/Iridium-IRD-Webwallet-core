import {Component, Input, OnInit} from '@angular/core';
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

  public InstanceStatus = InstanceStatus;

  constructor() {
  }

  ngOnInit(): void {
  }

}
