import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {CustomerProfileService} from '../../../services/customer-profile.service';
import {CustomerProfile} from '../../../dtos/customer-profile';
import {TranslateService} from '@ngx-translate/core';
import {ToastrService} from 'ngx-toastr';
import {NavbarService} from "../../../services/navbar.service";

@Component({
  selector: 'app-customer-profile',
  templateUrl: './customer-profile.component.html',
  styleUrl: './customer-profile.component.scss',
  standalone: false
})
export class CustomerProfileComponent {
  profiles: CustomerProfile[] = [];
  loading = false;

  profileToDelete?: CustomerProfile;
  deleteLoading = false;
  deleteError?: string;

  constructor(private customerProfileService: CustomerProfileService, public translateService: TranslateService, private navbarService: NavbarService, private router: Router, private notification: ToastrService) {
  }

  ngOnInit(): void {
    this.loadProfiles();
  }

// Retrieves the list of customer profiles from the API and updates the loading state.
  loadProfiles(): void {
    this.loading = true;

    this.customerProfileService.getCustomerProfiles().subscribe({
      next: (data) => {
        this.profiles = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load profiles', err);
        this.loading = false;
      }
    })
  }

// Navigates the user to the page for creating a new customer profile.
  openCreatePage(): void {
    this.router.navigate(['/customer/profiles/create']);
  }
// Closes the navigation bar and redirects to the edit page for the selected profile.
  openEditPage(item: CustomerProfile): void {
    this.navbarService.close();
    this.router.navigate(['/customer/profiles/edit', item.id])
  }

  openDetailPage(item: CustomerProfile): void {
    this.router.navigate(['customer/profiles/view', item.id])
  }
// Closes the navigation bar and stages the selected profile for deletion in the dialog
  openDeleteDialog(item: CustomerProfile): void {
    this.navbarService.close();
    this.profileToDelete = item;
    this.deleteError = undefined;
  }

  cancelDelete(): void {
    this.deleteError = undefined;
    this.profileToDelete = undefined;
    this.deleteLoading = false;
  }
// Deletes the profile via the service, updates the local UI list, and shows a success notification.
  confirmDelete(): void {
    if (!this.profileToDelete) {
      return;
    }

    this.deleteLoading = true;
    this.deleteError = undefined;
    const deletedProfileName = this.profileToDelete.profileName;

    this.customerProfileService.delete(this.profileToDelete.id).subscribe({
      next: () => {
        this.profiles = this.profiles.filter(
          item => item.id !== this.profileToDelete?.id
        );

        this.profileToDelete = undefined;
        this.deleteLoading = false;
        const translatedMessage = this.translateService.instant('CUSTOMER_PROFILE.DELETE_SUCCESS', {
          name: deletedProfileName
        });
        this.notification.success(translatedMessage);
      },

      error: (err) => {
        console.error('Failed to delete profile', err);
        this.deleteError = 'Profile could not be deleted.';
        this.deleteLoading = false;
      }
    });
  }
}

