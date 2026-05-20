import {Component, OnInit} from '@angular/core';
import {Equipment} from '../../../dtos/equipment';
import {EquipmentService} from '../../../services/equipment.service';
import {TranslateService} from '@ngx-translate/core';
import {Router} from '@angular/router';
import {EquipmentType} from "../../../dtos/equipmenttype";
import {RentalStatus} from "../../../dtos/rentalstatus";
import {SkillLevel} from "../../../dtos/skilllevel";
import {EquipmentSearch} from '../../../dtos/equipment-search';

@Component({
  selector: 'app-inventory',
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss',
  standalone: false
})
export class InventoryComponent implements OnInit {

  equipment: Equipment[] = [];
  loading = false;

  equipmentToDelete?: Equipment;
  deleteLoading = false;
  deleteError?: string;

  modelFilter = '';
  typeFilter = null;
  statusFilter = null;
  skillFilter = null;
  priceSortDirection: 'asc' | 'desc' = 'asc';

  equipmentTypes = [
    EquipmentType.HELMET,
    EquipmentType.SKI,
    EquipmentType.POLE,
    EquipmentType.SKIBOOT,
    EquipmentType.SNOWBOARD,
    EquipmentType.SNOWBOARDBOOT,
  ];

  rentalStatuses = [
    RentalStatus.FREE,
    RentalStatus.RESERVED,
    RentalStatus.RENTED,
    RentalStatus.MAINTENANCE
  ];

  skillLevels = [
    SkillLevel.BEGINNER,
    SkillLevel.INTERMEDIATE,
    SkillLevel.ADVANCED
  ];

  constructor(private equipmentService: EquipmentService, public translateService: TranslateService, private router: Router) {
  }

  ngOnInit(): void {
    this.loadEquipment();
  }

  loadEquipment(): void {
    this.loading = true;

    this.equipmentService.getAll().subscribe({
      next: (data) => {
        this.equipment = this.sortByPrice(data);
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
        this.deleteError = 'Equipment could not be deleted.';
        this.deleteLoading = false;
      }
    });
  }

  getStatusClass(status: string): string {
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

  searchEquipment(): void {
    this.loading = true;

    const searchRequest: EquipmentSearch = {
      model: this.modelFilter.trim() || undefined,
      type: this.typeFilter ?? undefined,
      status: this.statusFilter ?? undefined,
      targetSkillLevel: this.skillFilter ?? undefined,
    };

    this.equipmentService.search(searchRequest).subscribe({
      next: (data) => {
        this.equipment = this.sortByPrice(data);
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to search equipment', err);
        this.loading = false;
      }
    });
  }

  private sortByPrice(items: Equipment[]): Equipment[] {
    if (!this.priceSortDirection) {
      return items;
    }

    return [...items].sort((a, b) =>
      this.priceSortDirection === 'asc' ? a.price - b.price : b.price - a.price
    );
  }

  onPriceSortChange(): void {
    this.equipment = this.sortByPrice(this.equipment);
  }

  clearFilters(): void {
    this.modelFilter = '';
    this.typeFilter = null;
    this.statusFilter = null;
    this.skillFilter = null;
    this.priceSortDirection = 'asc';
    this.loadEquipment();
  }
}
