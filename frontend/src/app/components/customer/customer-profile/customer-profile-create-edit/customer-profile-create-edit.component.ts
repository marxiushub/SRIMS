import {Component} from '@angular/core';
import {CustomerService} from "../../../../services/customer.service";
import {CustomerProfileCreation} from "../../../../dtos/customer-profile-creation";
import {SkillLevel} from "../../../../dtos/skilllevel";
import {ActivatedRoute, Router} from "@angular/router";

export enum ProfileCreateEditMode {
  create,
  edit
};

@Component({
  selector: 'app-customer-profile-create-edit',
  templateUrl: './customer-profile-create-edit.component.html',
  styleUrl: './customer-profile-create-edit.component.scss',
  standalone: false
})
export class CustomerProfileCreateEditComponent {
  readonly ProfileCreateEditMode = ProfileCreateEditMode;

  mode: ProfileCreateEditMode = ProfileCreateEditMode.create;
  loading = false;
  error = false;

  profile: CustomerProfileCreation = {
    height: 0,
    profileName: '',
    shoeSize: 0,
    skillLevel: SkillLevel.BEGINNER,
    weight: 0,
    customerId: 0
  };

  skillLevels = [
    SkillLevel.BEGINNER,
    SkillLevel.INTERMEDIATE,
    SkillLevel.ADVANCED
  ]

  constructor(private customerService: CustomerService, private router: Router, private route: ActivatedRoute) {
  }

  onSubmit(): void {
    this.loading = true;
    this.error = false;

    if (this.mode === ProfileCreateEditMode.create) {
      //TODO implement create once the backend endpoint exists
      /*const request: EquipmentCreation = this.buildCreateRequest();

      this.customerService.create(request).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/customer/profiles']);
        },
        error: err => {
          console.error('Failed to create equipment', err);
          this.error = true;
          this.loading = false;
        }
      });*/
    } else {
      //TODO implement update once the backend endpoint exists
      /*const request: EquipmentUpdate = this.buildUpdateRequest();

      this.customerService.update(this.equipmentId!, request).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/customer/profiles']);
        },
        error: err => {
          console.error('Failed to update profile', err);
          this.error = true;
          this.loading = false;
        }
      });*/
    }
  }

  cancel(): void {
    this.router.navigate(['/customer/profiles']);
  }
}
