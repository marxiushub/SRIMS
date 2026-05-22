import {SkillLevel} from "./skilllevel";

export interface CustomerProfileCreation {
  height: number;
  profileName: string;
  shoeSize: number;
  skillLevel: SkillLevel;
  weight: number;
  customerId: number;
}
