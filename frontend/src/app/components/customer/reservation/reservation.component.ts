import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReservationDetail } from '../../../dtos/reservation-detail';
import { CustomerProfile } from '../../../dtos/customer-profile';
import { ReservationSearch } from '../../../dtos/reservation-search';
import { ReservationService } from '../../../services/reservation.service';
import { CustomerProfileService } from '../../../services/customer-profile.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-reservation',
  templateUrl: './reservation.component.html',
  styleUrls: ['./reservation.component.scss'],
  standalone: false
})
export class ReservationComponent implements OnInit {

  reservations: ReservationDetail[] = [];
  customerProfiles: CustomerProfile[] = [];
  loading = false;

  reservationToDelete?: ReservationDetail;
  deleteLoading = false;
  deleteError?: string;

  profileFilter: number | null = null;
  dateFilter: string = '';
  timeFilter: string = '';

  itemLimit: number = 10;
  currentPage: number = 1;

  //TODO: Remove hard-coded AccuntId once proper Account-system exists
  private readonly hardcodedAccountId = 1;

  constructor(
    private reservationService: ReservationService,
    private customerProfileService: CustomerProfileService,
    public translateService: TranslateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCustomerProfiles();
    this.loadReservations();
  }

  /**
   * Loads all customer Profiles of account, to show them in filter-dropdown.
   */
  loadCustomerProfiles(): void {
    this.customerProfileService.getCustomerProfiles(this.hardcodedAccountId).subscribe({
      next: (profiles) => {
        this.customerProfiles = profiles;
      },
      error: (err) => {
        console.error('Failed to load customer profiles for filters', err);
      }
    });
  }

  /**
   * Loads all reservations for own accountId.
   */
  loadReservations(): void {
    this.loading = true;

    const searchRequest: ReservationSearch = {
      accountId: this.hardcodedAccountId
    };

    this.reservationService.search(searchRequest).subscribe({
      next: (data) => {
        this.reservations = data;
        this.currentPage = 1;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load reservations', err);
        this.loading = false;
      }
    });
  }

  /**
   * Filters reservations for own accountId based on user input.
   */
  searchReservations(): void {
    this.loading = true;

    const searchRequest: ReservationSearch = {
      accountId: this.hardcodedAccountId,
      customerProfileId: this.profileFilter ?? undefined,
      pickUpDate: this.dateFilter || undefined,
      pickUpTime: this.timeFilter ? this.timeFilter + ':00' : undefined
    };

    this.reservationService.search(searchRequest).subscribe({
      next: (data) => {
        this.currentPage = 1;
        this.reservations = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to search reservations', err);
        this.loading = false;
      }
    });
  }

  clearFilters(): void {
    this.profileFilter = null;
    this.dateFilter = '';
    this.timeFilter = '';
    this.loadReservations();
  }

  openCreatePage(): void {
    this.router.navigate(['/customer/reservation/create']);
  }

  openDetailPage(item: ReservationDetail): void {
    this.router.navigate(['/customer/reservation/view', item.id]);
  }

  openEditPage(item: ReservationDetail): void {
    this.router.navigate(['/customer/reservation/edit', item.id]);
  }

  openDeleteDialog(item: ReservationDetail): void {
    this.reservationToDelete = item;
    this.deleteError = undefined;
  }

  cancelDelete(): void {
    this.deleteError = undefined;
    this.reservationToDelete = undefined;
    this.deleteLoading = false;
  }

  confirmDelete(): void {
    if (!this.reservationToDelete) {
      return;
    }

    this.deleteLoading = true;
    this.deleteError = undefined;

    this.reservationService.delete(this.reservationToDelete.id).subscribe({
      next: () => {
        this.reservations = this.reservations.filter(
          item => item.id !== this.reservationToDelete?.id
        );

        if (this.startIndex > this.reservations.length && this.currentPage > 1) {
          this.currentPage--;
        }

        this.reservationToDelete = undefined;
        this.deleteLoading = false;
      },
      error: (err) => {
        console.error('Failed to delete reservation', err);
        this.deleteError = err.error?.message || 'Reservation could not be deleted.';
        this.deleteLoading = false;
      }
    });
  }

  // Pagination helper-methods (copied 1:1 from inventory.component.ts)
  get startIndex(): number {
    return (this.currentPage - 1) * this.itemLimit + 1;
  }

  get endIndex(): number {
    const endIndex = this.currentPage * this.itemLimit;
    return endIndex > this.reservations.length ? this.reservations.length : endIndex;
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage(): void {
    if (this.endIndex < this.reservations.length) {
      this.currentPage++;
    }
  }

  goToFirstPage(): void {
    this.currentPage = 1;
  }

  goToLastPage(): void {
    this.currentPage = Math.ceil(this.reservations.length / this.itemLimit);
  }

  onItemLimitChange(): void {
    this.currentPage = 1;
  }
}
