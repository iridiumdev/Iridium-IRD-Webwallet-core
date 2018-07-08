import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {TranslateModule} from "@ngx-translate/core";
import {FormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {FlexLayoutModule} from "@angular/flex-layout";

@NgModule({
  imports: [
    CommonModule
  ],
  exports: [
    CommonModule,
    TranslateModule,
    FormsModule,

    MatButtonModule,
    MatIconModule,
    FlexLayoutModule
  ]
})
export class SharedModule {
}
