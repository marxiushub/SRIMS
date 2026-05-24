import {SkillLevel} from "./skilllevel";

export interface CustomerProfileCreationUpdate {
  height: number;
  profileName: string;
  shoeSize: number;
  skillLevel: SkillLevel;
  weight: number;
  customerId: number;
}
