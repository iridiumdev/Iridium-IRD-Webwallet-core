import {NgModule} from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {HomeLayoutComponent} from "../layout/home-layout/home-layout.component";
import {SimpleAuthGuard} from "@elderbyte/ngx-jwt-auth";
import {WalletListComponent} from "./wallet-list/wallet-list.component";
import {WalletOverviewComponent} from "./wallet-overview/wallet-overview.component";
import {WalletCreateComponent} from "./wallet-create/wallet-create.component";
import {WalletImportComponent} from "./wallet-import/wallet-import.component";

const routes: Routes = [
  {
    path: 'wallet',
    component: HomeLayoutComponent,
    canActivate: [SimpleAuthGuard],
    children: [
      {path: '', redirectTo: '/wallet/list', pathMatch: 'full'},
      {path: 'list', component: WalletListComponent},
      {path: 'create', component: WalletCreateComponent},
      {path: 'import', component: WalletImportComponent},
      {path: ':id/overview', component: WalletOverviewComponent},
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class WalletRoutingModule {
}
