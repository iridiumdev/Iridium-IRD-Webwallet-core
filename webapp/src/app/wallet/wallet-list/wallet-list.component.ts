import { Component, OnInit } from '@angular/core';
import {WalletService} from "../_service/wallet.service";
import {WalletContainer} from "../_model/wallet-container";

@Component({
  selector: 'app-wallet-list',
  templateUrl: './wallet-list.component.html',
  styleUrls: ['./wallet-list.component.scss']
})
export class WalletListComponent implements OnInit {


  public walletList: WalletContainer[];

  constructor(private walletService: WalletService) { }

  ngOnInit() {
    this.walletService.getWalletList().subscribe(list => {
      this.walletList = list
    })
  }

}
