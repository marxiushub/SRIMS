import {Component, OnInit} from '@angular/core';
import {EquipmentService} from '../../../services/equipment.service';
import {EquipmentOverview} from '../../../dtos/equipment-overview';
import {EquipmentType} from '../../../dtos/equipmenttype';
import {RentalStatus} from '../../../dtos/rentalstatus';

const DONUT_RADIUS = 48;
const DONUT_CIRCUMFERENCE = 2 * Math.PI * DONUT_RADIUS;

interface TypeDonut {
  type: EquipmentType;
  freeCount: number;
  rentedCount: number;
  maintenanceCount: number;
  total: number;
  freeDashArray: string;
  freeDashOffset: number;
  rentedDashArray: string;
  rentedDashOffset: number;
  maintenanceDashArray: string;
  maintenanceDashOffset: number;
}

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss',
  standalone: false
})
export class OverviewComponent implements OnInit {

  overview: EquipmentOverview | null = null;
  loading = false;
  errorMessageKey: string | null = null;

  typeDonuts: TypeDonut[] = [];
  totalFree = 0;
  totalRented = 0;
  totalMaintenance = 0;

  constructor(private equipmentService: EquipmentService) {
  }

  ngOnInit(): void {
    this.loadOverview();
  }

  loadOverview(): void {
    this.loading = true;
    this.errorMessageKey = null;

    this.equipmentService.getStatusOverview().subscribe({
      next: (data) => {
        this.overview = data;
        this.typeDonuts = this.buildTypeDonuts(data.counts);
        this.totalFree = this.typeDonuts.reduce((sum, donut) => sum + donut.freeCount, 0);
        this.totalRented = this.typeDonuts.reduce((sum, donut) => sum + donut.rentedCount, 0);
        this.totalMaintenance = this.typeDonuts.reduce((sum, donut) => sum + donut.maintenanceCount, 0);
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load equipment status overview', err);
        this.errorMessageKey = 'STAFF.OVERVIEW.MESSAGES.ERROR';
        this.loading = false;
      }
    });
  }

  private buildTypeDonuts(counts: Record<EquipmentType, Record<RentalStatus, number>> | null): TypeDonut[] {
    if (!counts) {
      return [];
    }

    return Object.values(EquipmentType).map((type) => {
      const statusCounts = counts[type];
      const freeCount = statusCounts?.[RentalStatus.FREE] ?? 0;
      const rentedCount = statusCounts?.[RentalStatus.RENTED] ?? 0;
      const maintenanceCount = statusCounts?.[RentalStatus.MAINTENANCE] ?? 0;
      const total = freeCount + rentedCount + maintenanceCount;

      const freeLength = total > 0 ? (freeCount / total) * DONUT_CIRCUMFERENCE : 0;
      const rentedLength = total > 0 ? (rentedCount / total) * DONUT_CIRCUMFERENCE : 0;
      const maintenanceLength = total > 0 ? (maintenanceCount / total) * DONUT_CIRCUMFERENCE : 0;

      return {
        type,
        freeCount,
        rentedCount,
        maintenanceCount,
        total,
        freeDashArray: `${freeLength} ${DONUT_CIRCUMFERENCE}`,
        freeDashOffset: 0,
        rentedDashArray: `${rentedLength} ${DONUT_CIRCUMFERENCE}`,
        rentedDashOffset: -freeLength,
        maintenanceDashArray: `${maintenanceLength} ${DONUT_CIRCUMFERENCE}`,
        maintenanceDashOffset: -(freeLength + rentedLength),
      };
    });
  }
}
