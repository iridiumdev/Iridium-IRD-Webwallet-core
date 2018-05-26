import { Component, OnInit } from '@angular/core';
import {AuthService} from "@elderbyte/ngx-jwt-auth";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  username: string;
  password: string;

  constructor(private authService: AuthService) { }

  login() {
    this.authService
      .loginWithCredentials(this.username, this.password)
      .subscribe(principal => {
        console.log(principal);
      }, error => {
        console.log(error);
      });
  }
}
