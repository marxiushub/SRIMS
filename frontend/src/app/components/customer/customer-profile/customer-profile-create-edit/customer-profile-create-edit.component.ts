import {Component} from '@angular/core';
import {CustomerProfileService} from "../../../../services/customer-profile.service";
import {CustomerProfileCreationUpdate} from "../../../../dtos/customer-profile-creation-update";
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
  profileId?: number;
  loading = false;
  error = false;

  profile: CustomerProfileCreationUpdate = {
    height: 0,
    profileName: '',
    shoeSize: 0,
    skillLevel: SkillLevel.BEGINNER,
    weight: 0,
    customerId: 1, //Customer ID hardcoded to 1 until accounts are implemented
  };

  skillLevels = [
    SkillLevel.BEGINNER,
    SkillLevel.INTERMEDIATE,
    SkillLevel.ADVANCED
  ]

  constructor(private customerProfileService: CustomerProfileService, private router: Router, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.mode = ProfileCreateEditMode.edit;
      this.profileId = Number(id);
      this.loadProfile(this.profileId);
    }
  }

  private loadProfile(id: number): void {
    this.loading = true;
    this.customerProfileService.getById(id).subscribe({
      next: (data) => {
        this.profile = {
          height: data.height,
          profileName: data.profileName,
          shoeSize: data.shoeSize,
          skillLevel: data.skillLevel,
          weight: data.weight,
          customerId: 1, //Customer ID hardcoded to 1 until accounts are implemented
        };

        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load profile', err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    this.loading = true;
    this.error = false;

    const request: CustomerProfileCreationUpdate = {
      height: this.profile.height,
      profileName: this.profile.profileName,
      shoeSize: this.profile.shoeSize,
      skillLevel: this.profile.skillLevel,
      weight: this.profile.weight,
      customerId: 1, //Customer ID hardcoded to 1 until accounts are implemented
    };

    if (this.mode === ProfileCreateEditMode.create) {
      this.customerProfileService.create(request).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/customer/profiles']);
        },
        error: err => {
          console.error('Failed to create profile', err);
          this.error = true;
          this.loading = false;
        }
      });
    } else {
      this.customerProfileService.update(this.profileId!, request).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/customer/profiles']);
        },
        error: err => {
          console.error('Failed to update profile', err);
          this.error = true;
          this.loading = false;
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/customer/profiles']);
  }
}
