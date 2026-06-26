import {Component} from '@angular/core';
import {CustomerProfileService} from "../../../../services/customer-profile.service";
import {TranslateService} from '@ngx-translate/core';
import {CustomerProfileCreationUpdate} from "../../../../dtos/customer-profile-creation-update";
import {SkillLevel} from "../../../../dtos/skilllevel";
import {ActivatedRoute, Router} from "@angular/router";
import {NgForm, NgModel} from "@angular/forms";
import {ToastrService} from 'ngx-toastr';
import { ErrorMappingService } from '../../../../services/error-mapping.service';

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
  loadError = false;
  loadErrorMessage = '';
  error = false;
  errorMessage = '';
  submitted = false;

  profile: CustomerProfileCreationUpdate = {
    height: null,
    profileName: '',
    shoeSize: null,
    skillLevel: null,
    weight: null
  };

  skillLevels = [
    SkillLevel.BEGINNER,
    SkillLevel.INTERMEDIATE,
    SkillLevel.ADVANCED
  ]

  constructor(
    private customerProfileService: CustomerProfileService,
    public translateService: TranslateService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private errorMapping: ErrorMappingService
  ) {
  }

  /**
  * Initializes the component and switches to edit mode if a profile ID is present in the route.
  */
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.mode = ProfileCreateEditMode.edit;
      this.profileId = Number(id);
      this.loadProfile(this.profileId);
    }
  }

  /**
   * Loads a customer profile from the backend and maps it into the form model.
   */
  private loadProfile(id: number): void {
    this.loading = true;
    this.loadError = false;
    this.customerProfileService.getById(id).subscribe({
      next: (data) => {
        this.profile = {
          height: data.height,
          profileName: data.profileName,
          shoeSize: data.shoeSize,
          skillLevel: data.skillLevel,
          weight: data.weight
        };

        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load profile', err);
        this.loadError = true;
        this.loading = false;
        this.loadErrorMessage = this.errorMapping.getErrorMessage(err);
      }
    });
  }

  /**
   * Submits the form to either create a new profile or update an existing one.
   */
  onSubmit(form?: NgForm): void {
    this.submitted = true;
    this.loading = true;
    this.loadError = false;
    this.error = false;

    if (form) {
      form.control.markAllAsTouched();
      if (form.invalid) {
        this.loading = false;
        return;
      }
    }

    const request: CustomerProfileCreationUpdate = {
      height: this.profile.height,
      profileName: this.profile.profileName,
      shoeSize: this.profile.shoeSize,
      skillLevel: this.profile.skillLevel,
      weight: this.profile.weight
    };

    if (this.mode === ProfileCreateEditMode.create) {
      this.customerProfileService.create(request).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/customer/profiles']);
          const translatedMessage = this.translateService.instant('CUSTOMER_PROFILE_CREATE.SUCCESS', {
            name: this.profile.profileName
          });
          this.notification.success(translatedMessage);
        },
        error: err => {
          console.error('Failed to create profile', err);
          this.error = true;
          this.loading = false;
          this.errorMessage = this.errorMapping.getErrorMessage(err);
        }
      });
    } else {
      this.customerProfileService.update(this.profileId!, request).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/customer/profiles']);
          const translatedMessage = this.translateService.instant('CUSTOMER_PROFILE_EDIT.SUCCESS', {
            name: this.profile.profileName
          });
          this.notification.success(translatedMessage);
        },
        error: err => {
          console.error('Failed to update profile', err);
          this.error = true;
          this.loading = false;
          this.errorMessage = this.errorMapping.getErrorMessage(err);
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/customer/profiles']);
  }

  isInvalid(control: NgModel | null | undefined): boolean {
    return !!control && control.invalid === true && (control.touched || this.submitted);
  }

  hasError(control: NgModel | null | undefined, errorName: string): boolean {
    return !!control && control.hasError(errorName) && (control.touched || this.submitted);
  }
}
