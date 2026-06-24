import {Component, Input} from '@angular/core';
import {EquipmentType} from '../../../../dtos/equipmenttype';
import {RentalStatus} from '../../../../dtos/rentalstatus';

interface TypeTile {
  type: EquipmentType;
  freeCount: number;
  rentedCount: number;
  maintenanceCount: number;
  total: number;
}

@Component({
  selector: 'app-overview-tiles',
  templateUrl: './overview-tiles.component.html',
  styleUrl: './overview-tiles.component.scss',
  standalone: false
})
export class OverviewTilesComponent {

  private _counts: Record<EquipmentType, Record<RentalStatus, number>> | null = null;
  typeTiles: TypeTile[] = [];
  totalFree = 0;
  totalRented = 0;
  totalMaintenance = 0;

  @Input()
  set counts(value: Record<EquipmentType, Record<RentalStatus, number>> | null) {
    this._counts = value;
    this.typeTiles = this.buildTypeTiles(value);
    this.totalFree = this.typeTiles.reduce((sum, tile) => sum + tile.freeCount, 0);
    this.totalRented = this.typeTiles.reduce((sum, tile) => sum + tile.rentedCount, 0);
    this.totalMaintenance = this.typeTiles.reduce((sum, tile) => sum + tile.maintenanceCount, 0);
  }

  get counts(): Record<EquipmentType, Record<RentalStatus, number>> | null {
    return this._counts;
  }

  private buildTypeTiles(counts: Record<EquipmentType, Record<RentalStatus, number>> | null): TypeTile[] {
    if (!counts) {
      return [];
    }

    return Object.values(EquipmentType).map((type) => {
      const statusCounts = counts[type];
      const freeCount = statusCounts?.[RentalStatus.FREE] ?? 0;
      const rentedCount = statusCounts?.[RentalStatus.RENTED] ?? 0;
      const maintenanceCount = statusCounts?.[RentalStatus.MAINTENANCE] ?? 0;

      return {
        type,
        freeCount,
        rentedCount,
        maintenanceCount,
        total: freeCount + rentedCount + maintenanceCount,
      };
    });
  }
}
