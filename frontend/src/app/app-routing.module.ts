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

const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  {path: 'message', canActivate: mapToCanActivate([AuthGuard]), component: MessageComponent},

  {path: 'staff', component: StaffComponent, children: [
      {path: 'inventory', component: InventoryComponent},
      {path: 'inventory/create', component: EquipmentCreateEditComponent, data: {mode: EquipmentCreateEditMode.create}},
      {path: 'inventory/edit/:id', component: EquipmentCreateEditComponent, data: {mode: EquipmentCreateEditMode.edit}},
      {path: 'inventory/view/:id', component: EquipmentViewComponent},
  ]},
  {path: 'customer', component: HomeComponent} //TODO: Replace later with actual component for customer
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
