import {SkillLevel} from "./skilllevel";

export interface CustomerProfileCreationUpdate {
  height: number | null;
  profileName: string;
  shoeSize: number | null;
  skillLevel: SkillLevel | null;
  weight: number | null;
}
