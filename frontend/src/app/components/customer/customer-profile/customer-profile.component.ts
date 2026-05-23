import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {CustomerService} from '../../../services/customer.service';
import {CustomerProfile} from '../../../dtos/customer-profile';
import {TranslateService} from '@ngx-translate/core';

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

  constructor(private customerService: CustomerService, public translateService: TranslateService, private router: Router) {
  }

  ngOnInit(): void {
    this.loadProfiles();
  }

  loadProfiles(): void {
    this.loading = true;

    this.customerService.getAll().subscribe({
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

  openCreatePage(): void {
    this.router.navigate(['/customer/profiles/create']);
  }

  openEditPage(item: CustomerProfile): void {
    this.router.navigate(['/customer/profiles/edit', item.id])
  }

  openDeleteDialog(item: CustomerProfile): void {
    this.profileToDelete = item;
    this.deleteError = undefined;
  }

  cancelDelete(): void {
    this.deleteError = undefined;
    this.profileToDelete = undefined;
    this.deleteLoading = false;
  }

  confirmDelete(): void {
    if (!this.profileToDelete) {
      return;
    }

    this.deleteLoading = true;
    this.deleteError = undefined;

    this.customerService.delete(this.profileToDelete.id).subscribe({
      next: () => {
        this.profiles = this.profiles.filter(
          item => item.id !== this.profileToDelete?.id
        );

        this.profileToDelete = undefined;
        this.deleteLoading = false;
      },

      error: (err) => {
        console.error('Failed to delete profile', err);
        this.deleteError = 'Profile could not be deleted.';
        this.deleteLoading = false;
      }
    });
  }
}

