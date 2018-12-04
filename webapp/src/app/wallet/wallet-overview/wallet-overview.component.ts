import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {switchMap} from "rxjs/operators";
import {WalletService} from "../_service/wallet.service";
import {InstanceStatus, Wallet} from "../_model/wallet";
import {FormBuilder, FormGroup} from "@angular/forms";
import {PasswordDto} from "../_model/password-dto";
import {DetailedWallet} from "../_model/detailed-wallet";

@Component({
  selector: 'app-wallet-overview',
  templateUrl: './wallet-overview.component.html',
  styleUrls: ['./wallet-overview.component.scss']
})
export class WalletOverviewComponent implements OnInit {

  @Input() wallet: DetailedWallet;

  walletLoaded: Boolean = false;
  walletId: string;

  public InstanceStatus = InstanceStatus;

  public form: FormGroup;

  public loading = false;


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private walletService: WalletService,
    private fb: FormBuilder
  ) {

    this.form = fb.group({
      password: ''
    } as PasswordDto);

  }

  ngOnInit() {
    this.route.paramMap
      .pipe(switchMap((params: ParamMap) => {
        this.walletId = params.get('id');
        return this.walletService.getDetailedWallet(params.get('id'))
      }))
      .subscribe(wallet => {
        this.walletLoaded = true;
        this.wallet = wallet
      }, () => {
        this.walletLoaded = false
      })
  }

  loadWallet() {
    const pwDto = (this.form.value as PasswordDto);

    this.loading = true;
    this.walletService.loadWallet(this.walletId, pwDto).subscribe(wallet => {
      this.walletLoaded = true;
      this.wallet = wallet;
    }, error => {
      console.error("Error!");
      console.error(error);
    }, () => {
      this.loading = false;
    })

  }

  lockWallet() {
    this.walletService.lockWallet(this.walletId).subscribe(() => {
      this.walletLoaded = false;
      this.wallet = null;
    }, error => {
      console.error("Error!");
      console.error(error);
    })
  }

}
