import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StatisticsRequestDto} from '../dtos/statistics-request';
import { StatisticsResponseDto} from '../dtos/statistics-response';
import {Globals} from '../global/globals';
@Injectable({
  providedIn: 'root'
})
export class StatisticsService {

  private statisticBaseUri: string = this.globals.backendUri + '/statistics';

  constructor(private httpClient: HttpClient, private globals: Globals) { }

  getEquipmentStatistics(requestData: StatisticsRequestDto): Observable<StatisticsResponseDto> {
    return this.httpClient.post<StatisticsResponseDto>(this.statisticBaseUri, requestData);
  }

}
