import {Component} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {CustomerProfile} from "../../../../dtos/customer-profile";
import {CustomerProfileService} from "../../../../services/customer-profile.service";
import {SkillLevel} from "../../../../dtos/skilllevel";
import {ErrorMappingService} from '../../../../services/error-mapping.service';

@Component({
  selector: 'app-customer-profile-details',
  templateUrl: './customer-profile-details.component.html',
  styleUrl: './customer-profile-details.component.scss',
  standalone: false
})
export class CustomerProfileDetailsComponent {

  profileId?: number;
  loading = false;
  loadError = false;
  loadErrorMessage = '';

  profileToDelete?: CustomerProfile;
  deleteLoading = false;
  deleteError?: string;

  profile: CustomerProfile = {
    id: 0,
    customerId: 0,
    profileName: '',
    height: 0,
    shoeSize: 0,
    skillLevel: SkillLevel.BEGINNER,
    weight: 0,
  };

  constructor(
    private customerProfileService: CustomerProfileService,
    private router: Router,
    private route: ActivatedRoute,
    private errorMapping: ErrorMappingService
  ) {
  }

// Retrieves the profile ID from the active route and initiates data loading on component startup.
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.profileId = Number(id);
      this.loadProfile(this.profileId);
    }
  }

  // Fetches the customer profile data from the API and manages loading/error states.
  loadProfile(id: number): void {
    this.loading = true;
    this.loadError = false;
    this.customerProfileService.getById(id).subscribe({
      next: (data) => {
        this.profile = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load profile', err);
        this.loadError = true;
        this.loading = false;
        this.loadErrorMessage = this.errorMapping.getErrorMessage(err);
      }
    })
  }

// Navigates the user back to the main customer profiles list.
  back(): void {
    this.router.navigate(['/customer/profiles']);
  }

// Redirects the user to the edit page for the currently loaded profile.
  openEditPage(): void {
    this.router.navigate(['/customer/profiles/edit', this.profileId]);
  }

// Sets the selected profile as the target for deletion and prepares the confirmation dialog.
  openDeleteDialog(item: CustomerProfile): void {
    this.profileToDelete = item;
    this.deleteError = undefined;
  }

  cancelDelete(): void {
    this.deleteError = undefined;
    this.profileToDelete = undefined;
    this.deleteLoading = false;
  }
// Executes the deletion of the targeted profile via the service and redirects upon success.
  confirmDelete(): void {
    if (!this.profileToDelete) {
      return;
    }

    this.deleteLoading = true;
    this.deleteError = undefined;

    this.customerProfileService.delete(this.profileToDelete.id).subscribe({
      next: () => {
        this.profileToDelete = undefined;
        this.deleteLoading = false;
        this.router.navigate(['/customer/profiles']);
      },

      error: (err) => {
        console.error('Failed to delete profile', err);
        this.deleteLoading = false;
        this.deleteError = this.errorMapping.getErrorMessage(err);
      }
    });
  }
}
