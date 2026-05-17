import {BrowserModule} from '@angular/platform-browser';
import {NgModule, provideZoneChangeDetection} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpClient, provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HeaderComponent} from './components/header/header.component';
import {FooterComponent} from './components/footer/footer.component';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {MessageComponent} from './components/message/message.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {httpInterceptorProviders} from './interceptors';
import {RouterModule} from "@angular/router";
import {InventoryComponent} from "./components/staff/inventory/inventory.component";
import {StaffComponent} from "./components/staff/staff.component";

import { TranslateModule, TranslateLoader } from '@ngx-translate/core';

import { registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';

registerLocaleData(localeDe, 'de');

export function HttpLoaderFactory(http: HttpClient): TranslateLoader{
  return {
    getTranslation(lang){
      return http.get(`./assets/i18n/${lang}.json`) as any;
    }
  };
}

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent,
    LoginComponent,
    MessageComponent,
    StaffComponent,
    InventoryComponent,
  ],
  bootstrap: [AppComponent],
  imports: [BrowserModule,
    AppRoutingModule,
    RouterModule,
    ReactiveFormsModule,
    NgbModule,
    FormsModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    })
  ],
  providers: [
    httpInterceptorProviders,
    provideHttpClient(withInterceptorsFromDi()),
    provideZoneChangeDetection(),
  ] })
export class AppModule {
}
