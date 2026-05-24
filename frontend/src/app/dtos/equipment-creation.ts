import {RentalStatus} from './rentalstatus';
import {SkillLevel} from './skilllevel';
import {EquipmentType} from "./equipmenttype";

export interface EquipmentCreation {
  type: EquipmentType;
  model: string;
  status: RentalStatus;
  targetSkillLevel: SkillLevel;
  price: number;

  length?: number;
  size?: number;
  soleLengthMm?: number;
  lacingSystem?: string;
}
