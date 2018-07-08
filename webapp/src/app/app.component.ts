import {Component, OnInit} from '@angular/core';
import {PrincipalService, TokenService} from "@elderbyte/ngx-jwt-auth";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{
  constructor(private tokenService: TokenService, private principalService: PrincipalService){}

  ngOnInit(): void {
    this.principalService.principal = this.tokenService.tryLogin();
  }
}
