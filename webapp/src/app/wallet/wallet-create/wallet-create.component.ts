import { Component, OnInit } from '@angular/core';
import {WalletService} from "../_service/wallet.service";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";
import {CreateDto} from "../_model/create-dto";

@Component({
  selector: 'app-wallet-create',
  templateUrl: './wallet-create.component.html',
  styleUrls: ['./wallet-create.component.scss']
})
export class WalletCreateComponent implements OnInit {

  public form: FormGroup;

  constructor(private walletService: WalletService,
              private fb: FormBuilder,
              private router: Router,
  ) {

    this.form = fb.group({
      name: '',
      password: '',
    } as CreateDto);

  }

  ngOnInit() {
  }

  createWallet() {
    const dto = (this.form.value as CreateDto);

    this.walletService.createWallet(dto).subscribe(wallet => {
      return this.router.navigateByUrl(`/wallet/${wallet.id}/overview`);
    }, error => {
      console.error("Error!");
      console.error(error);
    })

  }
}
