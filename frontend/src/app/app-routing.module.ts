import {NgModule} from '@angular/core';
import {mapToCanActivate, RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {LoginRegisterComponent, LoginRegisterMode} from './components/login-register/login-register.component';
import {AuthGuard} from './guards/auth.guard';
import {homeGuard} from './guards/home.guard';
import {MessageComponent} from './components/message/message.component';
import {StaffComponent} from './components/staff/staff.component';
import {InventoryComponent} from './components/staff/inventory/inventory.component';
import {
  EquipmentCreateEditComponent,
  EquipmentCreateEditMode
} from "./components/staff/inventory/equipment-create-edit/equipment-create-edit.component";
import {EquipmentViewComponent} from "./components/staff/inventory/equipment-view/equipment-view.component";
import {
  ReservationCreateEditComponent,
  ReservationCreateEditMode
} from "./components/customer/reservation/reservation-create-edit/reservation-create-edit.component";
import {BarcodeScannerComponent} from "./components/staff/barcodescanner/barcode-scanner.component";
import {CustomerComponent} from './components/customer/customer.component';
import {CustomerProfileComponent} from './components/customer/customer-profile/customer-profile.component';
import {
  CustomerProfileCreateEditComponent,
  ProfileCreateEditMode
} from './components/customer/customer-profile/customer-profile-create-edit/customer-profile-create-edit.component';
import {
  CustomerProfileDetailsComponent
} from './components/customer/customer-profile/customer-profile-details/customer-profile-details.component';
import {CustomerInventoryComponent} from "./components/customer/customer-inventory/customer-inventory.component";
import {
  CustomerEquipmentViewComponent
} from "./components/customer/customer-inventory/equipment-view/equipment-view.component";
import {ReservationComponent} from "./components/customer/reservation/reservation.component";
import {
  CustomerAccountComponent
} from './components/customer/customer-account/customer-account.component';
import {
  StaffAccountComponent
} from './components/staff/staff-account/staff-account.component';

import { StatisticsComponent } from './components/staff/statistics/statistics.component';
import {OverviewComponent} from './components/staff/overview/overview.component';

import {ReservationViewComponent} from "./components/customer/reservation/reservation-view/reservation-view.component";
import {StaffReservationComponent} from "./components/staff/reservation/staff-reservation.component";
import {
  StaffReservationViewComponent
} from "./components/staff/reservation/reservation-view/staff-reservation-view.component";
import {
  StaffReservationEditComponent
} from "./components/staff/reservation/reservation-edit/staff-reservation-edit.component";
import {ResetStaffPasswordComponent} from "./components/staff/reset-staff-password/reset-staff-password.component";
import {StaffGuard} from "./guards/staff.guard";
import {RegisterStaffComponent} from "./components/staff/register-staff/register-staff.component";


const routes: Routes = [
  {path: '', component: HomeComponent, canActivate: [homeGuard]},
  {path: 'login', component: LoginRegisterComponent, data: {mode: LoginRegisterMode.login}},
  {path: 'register', component: LoginRegisterComponent, data: {mode: LoginRegisterMode.register}},
  {path: 'forgot-password', component: LoginRegisterComponent, data: {mode: LoginRegisterMode.resetPassword}},
  {path: 'message', canActivate: mapToCanActivate([AuthGuard]), component: MessageComponent},

  {
    path: 'staff', component: StaffComponent, canActivate: [StaffGuard], children: [
      {path: 'inventory', component: InventoryComponent},
      {path: 'inventory/create', component: EquipmentCreateEditComponent, data: {mode: EquipmentCreateEditMode.create}},
      {path: 'inventory/edit/:id', component: EquipmentCreateEditComponent, data: {mode: EquipmentCreateEditMode.edit}},
      {path: 'inventory/view/:id', component: EquipmentViewComponent},
      {path: 'reservation', component: StaffReservationComponent},
      {path: 'reservation/edit/:id', component: StaffReservationEditComponent},
      {path: 'reservation/view/:id', component: StaffReservationViewComponent},
      {path: 'barcode-scanner', component: BarcodeScannerComponent},
      {path: 'statistics', component: StatisticsComponent},
      {path: 'account', component: StaffAccountComponent},
      {path: 'reset-staff-password', component: ResetStaffPasswordComponent},
      {path: 'overview', component: OverviewComponent},
      {path: 'register-staff', component: RegisterStaffComponent},
    ]
  },
  {
    path: 'customer', component: CustomerComponent, canActivate: [AuthGuard], children: [
      {path: 'reservation', component: ReservationComponent},
      {
        path: 'reservation/create',
        component: ReservationCreateEditComponent,
        data: {mode: ReservationCreateEditMode.create}
      },
      {
        path: 'reservation/edit/:id',
        component: ReservationCreateEditComponent,
        data: {mode: ReservationCreateEditMode.edit}
      },
      {path: 'reservation/view/:id', component: ReservationViewComponent},
      {path: 'inventory', component: CustomerInventoryComponent},
      {path: 'inventory/view/:id', component: CustomerEquipmentViewComponent},
      {path: 'account', component: CustomerAccountComponent}
    ]
  },
  {path: 'customer/profiles', component: CustomerProfileComponent},
  {
    path: 'customer/profiles/create',
    component: CustomerProfileCreateEditComponent,
    data: {mode: ProfileCreateEditMode.create}
  },
  {
    path: 'customer/profiles/edit/:id',
    component: CustomerProfileCreateEditComponent,
    data: {mode: ProfileCreateEditMode.edit}
  },
  {path: 'customer/profiles/view/:id', component: CustomerProfileDetailsComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
