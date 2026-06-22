import {EquipmentType} from "./equipmenttype";

export interface StatisticsRequestDto {
  searchStart: string;
  searchEnd: string;
  type?: EquipmentType|null;
  detailDegree: boolean;
}
