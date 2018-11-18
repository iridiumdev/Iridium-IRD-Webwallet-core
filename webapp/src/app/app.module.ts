import {BrowserModule} from '@angular/platform-browser';
import {NgModule, OnInit} from '@angular/core';

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
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {LoggerFactory, LogLevel} from "@elderbyte/ts-logger";
import {TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {FlexLayoutModule} from "@angular/flex-layout";
import {MAT_SNACK_BAR_DEFAULT_OPTIONS} from "@angular/material";
import {IsAuthenticatedDirective} from "./_directives/is-authenticated.directive";
import {WalletModule} from "./wallet/wallet.module";

// AoT requires an exported function for factories
export function HttpLoaderFactory(httpClient: HttpClient) {
  return new TranslateHttpLoader(httpClient, '/assets/i18n/', '.json');
}

@NgModule({
  declarations: [
    AppComponent,
    HomeLayoutComponent,
    IsAuthenticatedDirective,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,

    FlexLayoutModule,
    HttpClientModule,
    SimpleWebStorageModule,
    JwtAuthModule.forRoot({
      localLoginRoute: '/login',
      accessDeniedRoute: '/denied',
      obtainTokenUrl: '/auth/login',
      refresh: {
        strategy: RefreshStrategy.ONDEMAND,
        // interval: 10000,
        minTtl: 60000,
        url: '/auth/refresh',
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
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),

    HomeModule,
    WalletModule,

    //exported in SharedModule
    MatButtonModule,
    MatIconModule,

    //required for layouts
    MatToolbarModule,
    MatSidenavModule
  ],
  providers: [
    {provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: {duration: 3000}}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {

  constructor(public translate: TranslateService) {

    translate.addLangs(['en']);
    translate.setDefaultLang('en');
    translate.use('en');

    LoggerFactory.getDefaultConfiguration().withMaxLevel(LogLevel.Trace);

  }

}
