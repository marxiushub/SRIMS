import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Equipment } from '../../../../dtos/equipment';
import { EquipmentService } from '../../../../services/equipment.service';
import { EquipmentType } from '../../../../dtos/equipmenttype';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-equipment-detail',
  templateUrl: './equipment-view.component.html',
  styleUrl: './equipment-view.component.scss',
  standalone: false
})
//TODO: Once Reservation-System stands, also add all Reservation-Times to this detail view
export class EquipmentViewComponent implements OnInit {
  readonly EquipmentType = EquipmentType;

  equipment?: Equipment;
  loading = false;
  error = false;

  showDeleteModal = false;
  deleteLoading = false;
  deleteError?: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private equipmentService: EquipmentService,
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
      },
      error: (err) => {
        console.error('Failed to load equipment details', err);
        this.error = true;
        this.loading = false;
      }
    });
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
