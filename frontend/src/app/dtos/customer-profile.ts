import {SkillLevel} from "./skilllevel";

export interface CustomerProfile {
  id: number;
  height: number;
  profileName: string;
  shoeSize: number;
  skillLevel: SkillLevel;
  weight: number;
  customerId: number;
}
