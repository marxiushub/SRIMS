import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReservationService } from '../../../../services/reservation.service';
import { TranslateService } from '@ngx-translate/core';
import { ToastrService } from 'ngx-toastr';
import { ReservationDetail } from '../../../../dtos/reservation-detail';
import { ErrorMappingService } from '../../../../services/error-mapping.service';

@Component({
  selector: 'app-reservation-view',
  templateUrl: './reservation-view.component.html',
  styleUrls: ['./reservation-view.component.scss'],
  standalone: false
})
export class ReservationViewComponent implements OnInit {

  reservationId!: number;
  reservation?: ReservationDetail;
  loading: boolean = false;
  error?: string;

  showDeleteModal: boolean = false;
  deleteLoading: boolean = false;
  deleteError?: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reservationService: ReservationService,
    public translateService: TranslateService,
    private notification: ToastrService,
    private errorMapping: ErrorMappingService,
  ) { }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.reservationId = Number(idParam);
      this.loadReservationDetails();
    } else {
      this.error = 'RESERVATION.NOT_FOUND';
      this.notification.error('No valid reservation ID provided.');
    }
  }

  /**
   * Fetches the complete reservation information from the backend.
   */
  loadReservationDetails(): void {
    this.loading = true;
    this.error = undefined;

    this.reservationService.getById(this.reservationId).subscribe({
      next: (data: any) => {
        if (!data) {
          this.error = 'RESERVATION.NOT_FOUND';
        } else {
          this.reservation = data;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load reservation details', err);
        this.error = this.errorMapping.getErrorMessage(err);
        this.loading = false;
      }
    });
  }

  /**
   * Helper method to format time cleanly (HH:mm)
   */
  getFormattedTime(time?: string): string {
    if (!time) return '';
    return time.length > 5 ? time.substring(0, 5) : time;
  }

  /**
   * Helper to check if reservation is within 2 days of start
   */
  get isWithin2DaysOfStart(): boolean {
    if (!this.reservation?.startDate) return false;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const start = new Date(this.reservation.startDate);
    const diffDays = (start.getTime() - today.getTime()) / (1000 * 60 * 60 * 24);
    return diffDays < 2;
  }

  /**
   * Safe extraction of the equipment items array due to flexible naming.
   */
  get selectedEquipment(): any[] {
    if (!this.reservation) return [];
    return this.reservation.items || (this.reservation as any).equipment || (this.reservation as any).equipments || (this.reservation as any).equipmentList || [];
  }

  /**
   * Navigates back to the reservation overview list.
   */
  backToList(): void {
    this.router.navigate(['/customer/reservation']);
  }

  /**
   * Navigates directly to the edit page for this reservation.
   */
  openEditPage(): void {
    this.router.navigate(['/customer/reservation/edit', this.reservationId]);
  }

  // --- Delete Dialog Actions ---

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
        console.error('Failed to delete reservation from detail view', err);
        this.deleteLoading = false;
        this.deleteError = this.errorMapping.getErrorMessage(err);
      }
    });
  }
}
