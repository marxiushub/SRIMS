import {EquipmentType} from './equipmenttype';
import {RentalStatus} from './rentalstatus';

export interface EquipmentOverview {
  counts: Record<EquipmentType, Record<RentalStatus, number>>;
}
