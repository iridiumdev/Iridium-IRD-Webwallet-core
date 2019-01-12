import {NgModule} from '@angular/core';
import {WelcomeComponent} from './welcome/welcome.component';
import {HomeRoutingModule} from './home-routing.module';
import {LoginComponent} from './login/login.component';

import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TestComponent} from "./test/test.component";

import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from "@angular/material";
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {SharedModule} from "../shared/shared.module";


@NgModule({
  imports: [
    SharedModule,
    FormsModule,
    ReactiveFormsModule,
    HomeRoutingModule,

    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule
  ],
  declarations: [WelcomeComponent, LoginComponent, TestComponent]
})
export class HomeModule {
}
