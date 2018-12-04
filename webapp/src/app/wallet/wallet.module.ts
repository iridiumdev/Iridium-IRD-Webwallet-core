import {NgModule} from '@angular/core';
import {WalletListComponent} from './wallet-list/wallet-list.component';
import {WalletRoutingModule} from './wallet-routing.module';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatFormFieldModule, MatInputModule} from "@angular/material";
import {MatProgressBarModule} from '@angular/material/progress-bar';

import {SharedModule} from "../shared.module";
import {WalletListEntryComponent} from './wallet-list/wallet-list-entry/wallet-list-entry.component';
import { WalletOverviewComponent } from './wallet-overview/wallet-overview.component';
import { WalletCreateComponent } from './wallet-create/wallet-create.component';
import { WalletImportComponent } from './wallet-import/wallet-import.component';

@NgModule({
  imports: [
    SharedModule,
    WalletRoutingModule,

    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
  ],
  declarations: [WalletListComponent, WalletListEntryComponent, WalletOverviewComponent, WalletCreateComponent, WalletImportComponent]
})
export class WalletModule {
}
