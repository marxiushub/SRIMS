import { Component, OnInit } from '@angular/core';
import { Equipment } from '../../../dtos/equipment';
import { EquipmentService } from '../../../services/equipment.service';
import { TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-inventory',
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss',
  standalone: false
})
export class InventoryComponent implements OnInit{

  equipment: Equipment[] = [];
  loading = false;

  equipmentToDelete?: Equipment;
  deleteLoading = false;
  deleteError?: string;

  constructor (private equipmentService: EquipmentService, public translateService: TranslateService, private router: Router) { }

  ngOnInit(): void {
    this.loadEquipment();
  }

  loadEquipment(): void {
    this.loading = true;

    this.equipmentService.getAll().subscribe({
      next: (data) => {
        this.equipment = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load equipment', err);
        this.loading = false;
      }
    })
  }

  openCreatePage(): void {
    this.router.navigate(['/staff/inventory/create']);
  }

  openDetailPage(item: Equipment): void {
    this.router.navigate(['/staff/inventory/view', item.id]);
  }

  openEditPage(item: Equipment): void {
    this.router.navigate(['/staff/inventory/edit', item.id])
  }

  openDeleteDialog(item: Equipment): void {
    this.equipmentToDelete = item;
    this.deleteError = undefined;
  }

  cancelDelete(): void {
    this.deleteError = undefined;
    this.equipmentToDelete = undefined;
    this.deleteLoading = false;
  }

  confirmDelete(): void {
    if (!this.equipmentToDelete) {
      return;
    }

    this.deleteLoading = true;
    this.deleteError = undefined;

    this.equipmentService.delete(this.equipmentToDelete.id).subscribe({
      next: () => {
        this.equipment = this.equipment.filter(
          item => item.id !== this.equipmentToDelete?.id
        );

        this.equipmentToDelete = undefined;
        this.deleteLoading = false;
      },

      error: (err) => {
        console.error('Failed to delete equipment', err);
        this.deleteError = 'Eqipment could not be deleted.';
        this.deleteLoading = false;
      }
    });
  }

  getStatusClass(status: string):string {
    switch (status) {
      case 'FREE':
        return 'bg-success';
      case 'RESERVED':
        return 'bg-warning text-dark';
      case 'RENTED':
        return 'bg-danger';
      case 'MAINTENANCE':
        return 'bg-secondary';
      default:
        return 'bg-light text-dark';
    }
  }
}
