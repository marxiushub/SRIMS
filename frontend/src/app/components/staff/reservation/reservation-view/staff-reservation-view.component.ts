import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReservationService } from '../../../../services/reservation.service';
import { ReservationDetail } from '../../../../dtos/reservation-detail';
import { TranslateService } from '@ngx-translate/core';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-staff-reservation-view',
  templateUrl: './staff-reservation-view.component.html',
  styleUrls: ['./staff-reservation-view.component.scss'],
  standalone: false
})
export class StaffReservationViewComponent implements OnInit {
  reservationId!: number;
  reservation?: ReservationDetail;
  loading = false;
  error?: string;

  showDeleteModal: boolean = false;
  deleteLoading: boolean = false;
  deleteError?: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reservationService: ReservationService,
    public translateService: TranslateService,
    private notification: ToastrService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.reservationId = Number(id);
      this.loadDetails();
    } else {
      this.error = 'RESERVATION.NOT_FOUND';
      this.notification.error('No valid reservation ID provided.');
    }
  }

  /**
   * Fetches the complete reservation information from the backend.
   */
  loadDetails(): void {
    this.loading = true;
    this.error = undefined;
    this.reservationService.getById(this.reservationId).subscribe({
      next: (data) => {
        this.reservation = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load reservation details', err);
        this.error = 'RESERVATION.LOADING_FAILED';
        this.loading = false;
      }
    });
  }

  /**
   * Navigates back to the staff reservation list.
   */
  backToList(): void {
    this.router.navigate(['/staff/reservation']);
  }

  /**
   * Navigates to the staff edit component.
   */
  openEditPage(): void {
    this.router.navigate(['/staff/reservation/edit', this.reservationId]);
  }

  openDeleteDialog(): void {
    this.showDeleteModal = true;
    this.deleteError = undefined;
  }

  cancelDelete(): void {
    this.showDeleteModal = false;
    this.deleteError = undefined;
    this.deleteLoading = false;
  }

  confirmDelete(): void {
    if (!this.reservation) return;

    this.deleteLoading = true;
    this.deleteError = undefined;

    this.reservationService.delete(this.reservationId).subscribe({
      next: () => {
        this.showDeleteModal = false;
        this.deleteLoading = false;
        this.notification.success(this.translateService.instant('RESERVATION.DELETE_SUCCESS'));
        this.backToList();
      },
      error: (err) => {
        console.error('Failed to delete reservation from staff detail view', err);
        this.deleteError = err.error?.message || 'Reservation could not be deleted.';
        this.deleteLoading = false;
      }
    });
  }

  /**
   * Navigates to the equipment detail view.
   */
  viewEquipmentDetails(equipmentId: number): void {
    this.router.navigate(['/staff/inventory/view', equipmentId]);
  }
}
