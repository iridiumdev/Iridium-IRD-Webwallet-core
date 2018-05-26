import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WelcomeComponent } from './welcome/welcome.component';
import { HomeRoutingModule } from './home-routing.module';
import { LoginComponent } from './login/login.component';

import { FormsModule } from '@angular/forms';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    HomeRoutingModule
  ],
  declarations: [WelcomeComponent, LoginComponent]
})
export class HomeModule { }
