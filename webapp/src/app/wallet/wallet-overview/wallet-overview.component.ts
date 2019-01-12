import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {switchMap} from "rxjs/operators";
import {WalletService} from "../_service/wallet.service";
import {InstanceStatus} from "../_model/wallet";
import {FormBuilder, FormGroup} from "@angular/forms";
import {PasswordDto} from "../_model/password-dto";
import {DetailedWallet} from "../_model/detailed-wallet";
import {LoadingService} from "../../shared/loading.service";

@Component({
  selector: 'app-wallet-overview',
  templateUrl: './wallet-overview.component.html',
  styleUrls: ['./wallet-overview.component.scss']
})
export class WalletOverviewComponent implements OnInit {

  @Input() wallet: DetailedWallet;

  walletLoaded: Boolean = false;
  walletId: string;

  // noinspection JSUnusedGlobalSymbols
  public InstanceStatus = InstanceStatus;

  public form: FormGroup;


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private walletService: WalletService,
    private fb: FormBuilder,
    public loadingService: LoadingService,
  ) {

    this.form = fb.group({
      password: ''
    } as PasswordDto);

  }

  ngOnInit() {

    this.route.paramMap
      .pipe(switchMap((params: ParamMap) => {
        this.walletId = params.get('id');
        this.loadingService.isLoading = true;
        return this.walletService.getDetailedWallet(params.get('id'))
      }))
      .subscribe(wallet => {
        this.walletLoaded = true;
        this.wallet = wallet
      }, () => {
        this.walletLoaded = false
      }, () => {
        this.loadingService.isLoading = false;
      })
  }

  loadWallet() {
    const pwDto = (this.form.value as PasswordDto);

    this.loadingService.isLoading = true;
    this.walletService.loadWallet(this.walletId, pwDto)
      .subscribe(wallet => {
        this.walletLoaded = true;
        this.wallet = wallet;
      }, error => {
        // TODO: daniel 12.01.19 - handle error
        console.log("Error!");
        console.log(error);
      })
      .add(() => {
        this.loadingService.isLoading = false;
      })

  }

  lockWallet() {
    this.loadingService.isLoading = true;
    this.walletService.lockWallet(this.walletId).subscribe(() => {
      this.walletLoaded = false;
      this.wallet = null;
    }, error => {
      console.log("Error!");
      console.log(error);
    })
    .add(() => {
      this.loadingService.isLoading = false;
    })
  }

}
