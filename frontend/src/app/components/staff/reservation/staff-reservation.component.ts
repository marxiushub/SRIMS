import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReservationDetail } from '../../../dtos/reservation-detail';
import { ReservationSearch } from '../../../dtos/reservation-search';
import { CustomerSearch } from '../../../dtos/customer-search';
import { CustomerSearchResponse } from '../../../dtos/customer-search-response';
import { ReservationStatus } from '../../../dtos/reservationstatus';
import { ReservationService } from '../../../services/reservation.service';
import { StaffService } from '../../../services/staff.service';
import { TranslateService } from '@ngx-translate/core';
import { ToastrService } from 'ngx-toastr';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-staff-reservation',
  templateUrl: './staff-reservation.component.html',
  styleUrls: ['./staff-reservation.component.scss'],
  standalone: false
})
export class StaffReservationComponent implements OnInit {
  // Expose enum to the template
  readonly ReservationStatusEnum = ReservationStatus;

  reservations: ReservationDetail[] = [];
  loading = false;

  // Reservation filters
  dateFilter: string = '';
  timeFilter: string = '';
  statusFilter: ReservationStatus | null = null;

  // Customer filters for backend account lookups
  customerSearchCriteria: CustomerSearch = { firstName: '', lastName: '', email: '', userName: '' };
  foundCustomers: CustomerSearchResponse[] = [];
  selectedCustomerAccount: CustomerSearchResponse | null = null;
  selectedCustomerId: number | null = null;
  searchingCustomers = false;

  // RxJS subject to debounce background customer lookups while typing
  private customerSearch$ = new Subject<void>();

  reservationToDelete?: ReservationDetail;
  deleteLoading = false;
  deleteError?: string;

  itemLimit: number = 10;
  currentPage: number = 1;

  constructor(
    private reservationService: ReservationService,
    private staffService: StaffService,
    public translateService: TranslateService,
    private router: Router,
    private notification: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadReservations();

    // Set up debounced background search for customer accounts
    this.customerSearch$.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(() => {
      this.executeCustomerSearch();
    });
  }

  /**
   * Triggers the debounced customer search or clears filters if all inputs are empty.
   */
  onCustomerFilterChange(): void {
    if (!this.customerSearchCriteria.firstName &&
      !this.customerSearchCriteria.lastName &&
      !this.customerSearchCriteria.email &&
      !this.customerSearchCriteria.userName) {
      this.selectedCustomerAccount = null;
      this.selectedCustomerId = null;
      this.foundCustomers = [];
      this.loadReservations();
      return;
    }

    this.customerSearch$.next();
  }

  /**
   * Searches for customer accounts matching the given criteria in the backend.
   */
  executeCustomerSearch(): void {
    this.searchingCustomers = true;
    this.staffService.searchCustomers(this.customerSearchCriteria).subscribe({
      next: (customers) => {
        this.foundCustomers = customers;
        this.searchingCustomers = false;

        // Auto-select if exactly one unique customer matches
        if (customers.length === 1) {
          this.selectedCustomerAccount = customers[0];
          this.selectedCustomerId = customers[0].id ?? null;
          this.loadReservations();
        }
      },
      error: (err) => {
        console.error('Failed to search customer accounts', err);
        this.searchingCustomers = false;
      }
    });
  }

  /**
   * Handles manual selection of a customer account from the dropdown.
   */
  onCustomerSelectById(id: number | null): void {
    this.selectedCustomerId = id;
    this.selectedCustomerAccount = this.foundCustomers.find(c => c.id === id) || null;
    this.loadReservations();
  }

  /**
   * Loads all reservations from the backend conforming to all active search parameters.
   */
  loadReservations(): void {
    this.loading = true;

    const searchRequest: ReservationSearch = {
      startDate: this.dateFilter || undefined,
      pickUpTime: this.timeFilter ? this.timeFilter + ':00' : undefined,
      reservationStatus: this.statusFilter ?? undefined,
      accountId: this.selectedCustomerId ?? undefined
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
        this.notification.error('Failed to load reservations from server.');
      }
    });
  }

  clearAllFilters(): void {
    this.dateFilter = '';
    this.timeFilter = '';
    this.statusFilter = null;
    this.customerSearchCriteria = { firstName: '', lastName: '', email: '', userName: '' };
    this.foundCustomers = [];
    this.selectedCustomerAccount = null;
    this.selectedCustomerId = null;
    this.loadReservations();
  }

  openDetailPage(item: ReservationDetail): void {
    this.router.navigate(['/staff/reservation/view', item.id]);
  }

  openEditPage(item: ReservationDetail): void {
    this.router.navigate(['/staff/reservation/edit', item.id]);
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
    const deletedReservationId = this.reservationToDelete.id;

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
        const translatedMessage = this.translateService.instant('RESERVATION.DELETE_SUCCESS', {
          id: deletedReservationId
        });
        this.notification.success(translatedMessage);
      },
      error: (err) => {
        console.error('Failed to delete reservation', err);
        this.deleteError = err.error?.message || 'Reservation could not be deleted.';
        this.deleteLoading = false;
      }
    });
  }

  // Pagination Helper-Methods (copied 1:1 from inventory.component.ts)
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

  goToLastPage(): void {
    this.currentPage = Math.ceil(this.reservations.length / this.itemLimit);
  }
}
