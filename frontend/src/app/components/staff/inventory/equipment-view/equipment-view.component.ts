import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Equipment } from '../../../../dtos/equipment';
import { EquipmentService } from '../../../../services/equipment.service';
import { EquipmentType } from '../../../../dtos/equipmenttype';
import { TranslateService } from '@ngx-translate/core';

import { ReservationService } from '../../../../services/reservation.service';
import { ReservationDetail } from '../../../../dtos/reservation-detail';

@Component({
  selector: 'app-equipment-detail',
  templateUrl: './equipment-view.component.html',
  styleUrl: './equipment-view.component.scss',
  standalone: false
})

export class EquipmentViewComponent implements OnInit {
  readonly EquipmentType = EquipmentType;

  equipment?: Equipment;
  loading = false;
  error = false;

  reservations: ReservationDetail[] = [];
  reservationsLoading = false;
  reservationsError = false;

  showDeleteModal = false;
  deleteLoading = false;
  deleteError?: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private equipmentService: EquipmentService,
    private reservationService: ReservationService,
    public translateService: TranslateService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadEquipment(Number(id));
    }
  }

  private loadEquipment(id: number): void {
    this.loading = true;
    this.error = false;
    this.equipmentService.getById(id).subscribe({
      next: (data) => {
        this.equipment = data;
        this.loading = false;
        this.loadReservationsForEquipment(data.id);
      },
      error: (err) => {
        console.error('Failed to load equipment details', err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  private loadReservationsForEquipment(equipmentId: number): void {
    this.reservationsLoading = true;
    this.reservationsError = false;

    this.reservationService.search({equipmentIds: [equipmentId]}).subscribe({
      next: (data) => {
        this.reservations = data;
        this.reservationsLoading = false;
      },
      error: (err) => {
        console.error('Failed to load reservations details for equipment', err);
        this.reservationsError = true;
        this.reservationsLoading = false;
      }
    })
  }

  openEditPage(): void {
    if (this.equipment) {
      this.router.navigate(['/staff/inventory/edit', this.equipment.id]);
    }
  }

  openDeleteDialog(): void {
    this.showDeleteModal = true;
    this.deleteError = undefined;
  }

  cancelDelete(): void {
    this.showDeleteModal = false;
    this.deleteLoading = false;
    this.deleteError = undefined;
  }

  confirmDelete(): void {
    if (!this.equipment) return;

    this.deleteLoading = true;
    this.deleteError = undefined;

    this.equipmentService.delete(this.equipment.id).subscribe({
      next: () => {
        this.deleteLoading = false;
        this.showDeleteModal = false;
        this.router.navigate(['/staff/inventory']);
      },
      error: (err) => {
        console.error('Failed to delete equipment', err);
        this.deleteError = 'Equipment could not be deleted.';
        this.deleteLoading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/staff/inventory']);
  }

  //Helper-method to give RentalStatus-Enum-Values nice background coloring in HTML
  getStatusClass(status: string): string {
    switch (status) {
      case 'FREE': return 'bg-success';
      case 'RESERVED': return 'bg-warning text-dark';
      case 'RENTED': return 'bg-danger';
      case 'MAINTENANCE': return 'bg-secondary';
      default: return 'bg-light text-dark';
    }
  }
}
