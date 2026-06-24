import {BrowserModule} from '@angular/platform-browser';
import {NgModule, provideZoneChangeDetection} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpClient, provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HeaderComponent} from './components/header/header.component';
import {FooterComponent} from './components/footer/footer.component';
import {HomeComponent} from './components/home/home.component';
import {LoginRegisterComponent} from './components/login-register/login-register.component';
import {MessageComponent} from './components/message/message.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ToastrModule} from 'ngx-toastr';
import {httpInterceptorProviders} from './interceptors';
import {RouterModule} from "@angular/router";
import {InventoryComponent} from "./components/staff/inventory/inventory.component";
import {StaffComponent} from "./components/staff/staff.component";
import {
  EquipmentCreateEditComponent
} from "./components/staff/inventory/equipment-create-edit/equipment-create-edit.component";
import {CustomerComponent} from "./components/customer/customer.component";
import {CustomerProfileComponent} from "./components/customer/customer-profile/customer-profile.component";
import {
  CustomerProfileCreateEditComponent
} from "./components/customer/customer-profile/customer-profile-create-edit/customer-profile-create-edit.component";
import {
  CustomerProfileDetailsComponent
} from "./components/customer/customer-profile/customer-profile-details/customer-profile-details.component";
import {EquipmentViewComponent} from "./components/staff/inventory/equipment-view/equipment-view.component";
import {
  CustomerAccountComponent
} from "./components/customer/customer-account/customer-account.component";
import {
  StaffAccountComponent
} from "./components/staff/staff-account/staff-account.component";

import {
  ReservationCreateEditComponent
} from "./components/customer/reservation/reservation-create-edit/reservation-create-edit.component";

import {TranslateLoader, TranslateModule} from '@ngx-translate/core';

import {registerLocaleData} from '@angular/common';
import localeDe from '@angular/common/locales/de';
import {BarcodeScannerComponent} from "./components/staff/barcodescanner/barcode-scanner.component";
import {CustomerInventoryComponent} from "./components/customer/customer-inventory/customer-inventory.component";
import {
  CustomerEquipmentViewComponent
} from "./components/customer/customer-inventory/equipment-view/equipment-view.component";
import {ReservationComponent} from "./components/customer/reservation/reservation.component";
import {ZXingScannerModule} from '@zxing/ngx-scanner';

import { StatisticsComponent } from "./components/staff/statistics/statistics.component";

import {
  ReservationViewComponent
} from "./components/customer/reservation/reservation-view/reservation-view.component";
import { StaffReservationComponent } from './components/staff/reservation/staff-reservation.component';
import {
  StaffReservationViewComponent
} from "./components/staff/reservation/reservation-view/staff-reservation-view.component";
import {
  StaffReservationEditComponent
} from "./components/staff/reservation/reservation-edit/staff-reservation-edit.component";


registerLocaleData(localeDe, 'de');

export function HttpLoaderFactory(http: HttpClient): TranslateLoader {
  return {
    getTranslation(lang) {
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
    LoginRegisterComponent,
    MessageComponent,
    StaffComponent,
    InventoryComponent,
    EquipmentCreateEditComponent,
    EquipmentViewComponent,
    StaffReservationComponent,
    StaffReservationEditComponent,
    StaffReservationViewComponent,
    BarcodeScannerComponent,
    StaffAccountComponent,
    ReservationComponent,
    ReservationCreateEditComponent,
    ReservationViewComponent,
    CustomerComponent,
    CustomerInventoryComponent,
    CustomerEquipmentViewComponent,
    CustomerProfileComponent,
    CustomerProfileCreateEditComponent,
    CustomerProfileDetailsComponent,
    CustomerAccountComponent,
    StatisticsComponent,
  ],
  bootstrap: [AppComponent],
  imports: [BrowserModule,
    AppRoutingModule,
    RouterModule,
    ReactiveFormsModule,
    NgbModule,
    FormsModule,
    BrowserModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot(),
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
    ZXingScannerModule
  ],
  providers: [
    httpInterceptorProviders,
    provideHttpClient(withInterceptorsFromDi()),
    provideZoneChangeDetection(),
  ]
})
export class AppModule {
}
