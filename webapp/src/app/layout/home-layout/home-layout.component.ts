import { Component, OnInit } from '@angular/core';
import {AuthService, Principal, PrincipalService} from "@elderbyte/ngx-jwt-auth";
import {Router} from "@angular/router";
import {MatSnackBar} from "@angular/material";

@Component({
  selector: 'app-home-layout',
  templateUrl: './home-layout.component.html',
  styleUrls: ['./home-layout.component.scss']
})
export class HomeLayoutComponent implements OnInit {

  private principal: Principal;

  constructor(
    private principalService: PrincipalService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) { }

  public logout() {
    this.authService.logout();
    this.router.navigateByUrl('/').then(route => {
      this.snackBar.open("Logout successful!")
    });
  }


  ngOnInit() {
    this.principal = this.principalService.principalSnapshot;

    this.principalService.principalObservable.subscribe(p => {
      this.principal = p;
    })
  }

}
