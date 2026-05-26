import { Component, OnInit } from '@angular/core';
import { debounceTime, distinctUntilChanged, map, OperatorFunction } from 'rxjs';
import { Equipment } from '../../../dtos/equipment';
import { EquipmentService } from '../../../services/equipment.service';
import { TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { EquipmentType } from "../../../dtos/equipmenttype";
import { RentalStatus } from "../../../dtos/rentalstatus";
import { SkillLevel } from "../../../dtos/skilllevel";
import { EquipmentSearch } from '../../../dtos/equipment-search';

@Component({
  selector: 'app-customer-inventory',
  templateUrl: './customer-inventory.component.html',
  styleUrl: './customer-inventory.component.scss',
  standalone: false
})
export class CustomerInventoryComponent implements OnInit {

  equipment: Equipment[] = [];
  loading = false;

  modelOptions: string[] = [];
  modelFilter = '';
  typeFilter = null;
  statusFilter = null;
  skillFilter = null;
  priceSortDirection: 'asc' | 'desc' = 'asc';

  itemLimit: number = 10;
  currentPage: number = 1;

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

  constructor(
    private equipmentService: EquipmentService,
    public translateService: TranslateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadEquipment();
  }

  loadEquipment(): void {
    this.loading = true;
    this.equipmentService.getAll().subscribe({
      next: (data) => {
        this.buildModelOptions(data);
        this.equipment = this.sortByPrice(data);
        this.currentPage = 1;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load equipment', err);
        this.loading = false;
      }
    });
  }

  openDetailPage(item: Equipment): void {
    this.router.navigate(['/customer/inventory/view', item.id]);
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
        this.currentPage = 1;
        this.equipment = this.sortByPrice(data);
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to search equipment', err);
        this.loading = false;
      }
    });
  }

  searchModel: OperatorFunction<string, readonly string[]> = (text$) =>
    text$.pipe(
      debounceTime(150),
      distinctUntilChanged(),
      map((term) => {
        const q = term.trim().toLowerCase();
        return (q.length === 0
            ? this.modelOptions
            : this.modelOptions.filter((m) => m.toLowerCase().includes(q))
        ).slice(0, 10);
      })
    );

  private buildModelOptions(items: Equipment[]): void {
    this.modelOptions = [...new Set(items.map((e) => e.model).filter(Boolean))]
      .sort((a, b) => a.localeCompare(b));
  }

  private sortByPrice(items: Equipment[]): Equipment[] {
    if (!this.priceSortDirection) return items;
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

  get startIndex(): number { return (this.currentPage - 1) * this.itemLimit + 1; }
  get endIndex(): number {
    const endIndex = this.currentPage * this.itemLimit;
    return endIndex > this.equipment.length ? this.equipment.length : endIndex;
  }
  previousPage(): void { if (this.currentPage > 1) this.currentPage--; }
  nextPage(): void { if (this.endIndex < this.equipment.length) this.currentPage++; }
  goToFirstPage(): void { this.currentPage = 1; }
  goToLastPage(): void { this.currentPage = Math.ceil(this.equipment.length / this.itemLimit); }
  onItemLimitChange(): void { this.currentPage = 1; }
}
