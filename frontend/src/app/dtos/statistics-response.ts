export interface StatisticsResponseDto {
  detailDegree: boolean;

  itemCounts?: { [id: string]: number };

  modelCounts?: { [modelName: string]: number };
}
