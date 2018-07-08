import {Component} from '@angular/core';
import {AuthService} from "@elderbyte/ngx-jwt-auth";
import {FormBuilder, FormGroup} from "@angular/forms";
import {MatSnackBar} from "@angular/material";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  username: string;
  password: string;

  form: FormGroup;

  constructor(private authService: AuthService, fb: FormBuilder, private snackBar: MatSnackBar) {
    this.form = fb.group({
      username: '',
      password: '',
    });
  }

  login() {
    this.authService
      .loginWithCredentials(this.form.value.username, this.form.value.password)
      .subscribe(principal => {
        console.log(principal);
        this.snackBar.open(`Login successful! Welcome back, ${principal.username}.`);
      }, error => {
        this.snackBar.open('Login failed!');
        console.error(error);
      });
  }

}
