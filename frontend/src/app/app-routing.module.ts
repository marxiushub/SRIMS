import {NgModule} from '@angular/core';
import {mapToCanActivate, RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {AuthGuard} from './guards/auth.guard';
import {MessageComponent} from './components/message/message.component';
import { StaffComponent } from './components/staff/staff.component';
import { InventoryComponent } from './components/staff/inventory/inventory.component';
import { EquipmentCreateEditComponent, EquipmentCreateEditMode } from "./components/staff/inventory/equipment-create-edit/equipment-create-edit.component";
import { EquipmentViewComponent } from "./components/staff/inventory/equipment-view/equipment-view.component";
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

const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  {path: 'message', canActivate: mapToCanActivate([AuthGuard]), component: MessageComponent},

  {
    path: 'staff', component: StaffComponent, children: [
      {path: 'inventory', component: InventoryComponent},
      {path: 'inventory/create', component: EquipmentCreateEditComponent, data: {mode: EquipmentCreateEditMode.create}},
      {path: 'inventory/edit/:id', component: EquipmentCreateEditComponent, data: {mode: EquipmentCreateEditMode.edit}},
      {path: 'inventory/view/:id', component: EquipmentViewComponent},
      {path: 'barcode-scanner', component: BarcodeScannerComponent},
  ]},
  {path: 'customer', component: CustomerComponent},
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
