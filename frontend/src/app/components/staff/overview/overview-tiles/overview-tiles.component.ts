import {Component, Input} from '@angular/core';
import {EquipmentType} from '../../../../dtos/equipmenttype';
import {RentalStatus} from '../../../../dtos/rentalstatus';

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
  selector: 'app-overview-tiles',
  templateUrl: './overview-tiles.component.html',
  styleUrl: './overview-tiles.component.scss',
  standalone: false
})
export class OverviewTilesComponent {

  private _counts: Record<EquipmentType, Record<RentalStatus, number>> | null = null;
  typeDonuts: TypeDonut[] = [];
  totalFree = 0;
  totalRented = 0;
  totalMaintenance = 0;

  @Input()
  set counts(value: Record<EquipmentType, Record<RentalStatus, number>> | null) {
    this._counts = value;
    this.typeDonuts = this.buildTypeDonuts(value);
    this.totalFree = this.typeDonuts.reduce((sum, tile) => sum + tile.freeCount, 0);
    this.totalRented = this.typeDonuts.reduce((sum, tile) => sum + tile.rentedCount, 0);
    this.totalMaintenance = this.typeDonuts.reduce((sum, tile) => sum + tile.maintenanceCount, 0);
  }

  get counts(): Record<EquipmentType, Record<RentalStatus, number>> | null {
    return this._counts;
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
