import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HomeLayoutComponent} from './layout/home-layout/home-layout.component';
import {HomeModule} from "./home/home.module";


import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatSidenavModule} from '@angular/material/sidenav';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {SimpleWebStorageModule} from "@elderbyte/ngx-simple-webstorage";
import {JwtAuthModule, RefreshStrategy, StorageType} from "@elderbyte/ngx-jwt-auth";
import {HttpClientModule} from "@angular/common/http";
import {LoggerFactory, LogLevel} from "@elderbyte/ts-logger";


@NgModule({
  declarations: [
    AppComponent,
    HomeLayoutComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,

    HttpClientModule,
    SimpleWebStorageModule.forRoot(),
    JwtAuthModule.forRoot({
      localLoginRoute: '/login',
      accessDeniedRoute: '/denied',
      obtainTokenUrl: '/auth/token',
      refresh: {
        strategy: RefreshStrategy.ONDEMAND,
        interval: 10000
      },
      tokenStorage: {
        type: StorageType.LOCAL,
        accessTokenKeyName: 'access_token',
        refreshTokenKeyName: 'refresh_token'
      },
      jwt: {
        usernameField: 'sub',
        rolesField: 'authorities'
      }
    }),

    HomeModule,

    MatIconModule,
    MatButtonModule,
    MatToolbarModule,
    MatSidenavModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {

  constructor() {
    LoggerFactory.getDefaultConfiguration().withMaxLevel(LogLevel.Trace)
  }


}
