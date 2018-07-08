import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {WelcomeComponent} from "./welcome/welcome.component";
import {HomeLayoutComponent} from "../layout/home-layout/home-layout.component";
import {LoginComponent} from "./login/login.component";
import {TestComponent} from "./test/test.component";
import {SimpleAuthGuard} from "@elderbyte/ngx-jwt-auth";

const routes: Routes = [
  {
    path: '',
    component: HomeLayoutComponent,
    children: [
      { path: '', component: WelcomeComponent, pathMatch: 'full'},
      { path: 'login', component: LoginComponent},
      { path: 'test', canActivate: [SimpleAuthGuard], component: TestComponent}
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class HomeRoutingModule { }
