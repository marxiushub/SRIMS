import {RentalStatus} from './rentalstatus';
import {SkillLevel} from './skilllevel';
import {EquipmentType} from "./equipmenttype";

export interface Equipment {
  id: number;
  barcodeId: string;
  price: number;
  model: string;
  status: RentalStatus;
  targetSkillLevel: SkillLevel;
  equipmentType: EquipmentType;
}
