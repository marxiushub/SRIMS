import {RentalStatus} from './rentalstatus';
import {SkillLevel} from './skilllevel';
import {EquipmentType} from "./equipmenttype";

export interface EquipmentSearch {
  model?: string;
  type?: EquipmentType;
  status?: RentalStatus;
  targetSkillLevel?: SkillLevel;
}
