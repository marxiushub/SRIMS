import {Component, Input} from '@angular/core';
import {EquipmentType} from '../../../../dtos/equipmenttype';
import {RentalStatus} from '../../../../dtos/rentalstatus';

interface BarRow {
  type: EquipmentType;
  total: number;
  freePercent: number;
  rentedPercent: number;
  maintenancePercent: number;
  freeCount: number;
  rentedCount: number;
  maintenanceCount: number;
}

@Component({
  selector: 'app-overview-bar-chart',
  templateUrl: './overview-bar-chart.component.html',
  styleUrl: './overview-bar-chart.component.scss',
  standalone: false
})
export class OverviewBarChartComponent {

  private _counts: Record<EquipmentType, Record<RentalStatus, number>> | null = null;
  rows: BarRow[] = [];
  totalFree = 0;
  totalRented = 0;
  totalMaintenance = 0;

  @Input()
  set counts(value: Record<EquipmentType, Record<RentalStatus, number>> | null) {
    this._counts = value;
    this.rows = this.buildRows(value);
    this.totalFree = this.rows.reduce((sum, row) => sum + row.freeCount, 0);
    this.totalRented = this.rows.reduce((sum, row) => sum + row.rentedCount, 0);
    this.totalMaintenance = this.rows.reduce((sum, row) => sum + row.maintenanceCount, 0);
  }

  get counts(): Record<EquipmentType, Record<RentalStatus, number>> | null {
    return this._counts;
  }

  private buildRows(counts: Record<EquipmentType, Record<RentalStatus, number>> | null): BarRow[] {
    if (!counts) {
      return [];
    }

    return Object.values(EquipmentType).map((type) => {
      const statusCounts = counts[type];
      const freeCount = statusCounts?.[RentalStatus.FREE] ?? 0;
      const rentedCount = statusCounts?.[RentalStatus.RENTED] ?? 0;
      const maintenanceCount = statusCounts?.[RentalStatus.MAINTENANCE] ?? 0;
      const total = freeCount + rentedCount + maintenanceCount;

      return {
        type,
        total,
        freeCount,
        rentedCount,
        maintenanceCount,
        freePercent: total > 0 ? (freeCount / total) * 100 : 0,
        rentedPercent: total > 0 ? (rentedCount / total) * 100 : 0,
        maintenancePercent: total > 0 ? (maintenanceCount / total) * 100 : 0,
      };
    });
  }
}
