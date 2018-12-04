import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {WalletService} from "../_service/wallet.service";
import {Router} from "@angular/router";
import {CreateDto} from "../_model/create-dto";
import {ImportDto} from "../_model/import-dto";

@Component({
  selector: 'app-wallet-import',
  templateUrl: './wallet-import.component.html',
  styleUrls: ['./wallet-import.component.scss']
})
export class WalletImportComponent implements OnInit {

  public form: FormGroup;

  constructor(private walletService: WalletService,
              private fb: FormBuilder,
              private router: Router,
  ) {

    this.form = fb.group({
      name: '',
      password: '',
      viewSecretKey: '',
      spendSecretKey: '',
    } as ImportDto);

  }

  ngOnInit() {
  }

  createWallet() {
    const dto = (this.form.value as ImportDto);

    this.walletService.importWallet(dto).subscribe(wallet => {
      return this.router.navigateByUrl(`/wallet/${wallet.id}/overview`);
    }, error => {
      console.error("Error!");
      console.error(error);
    })

  }
}
