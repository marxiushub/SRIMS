import { RentalStatus } from './rentalstatus';
import { SkillLevel } from './skilllevel';
import { EquipmentType } from './equipmenttype';

export interface EquipmentUpdate {
  type: EquipmentType;
  model: string;
  status: RentalStatus;
  targetSkillLevel: SkillLevel;
  price: number;

  length: number | null;
  size: number | null;
  soleLengthMm: number | null;
  lancingSystem: string | null;
}
