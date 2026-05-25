import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';
import { ReservationCreation } from '../dtos/reservation-creation';
import { ReservationDetail } from '../dtos/reservation-detail';
import { ReservationSearch } from '../dtos/reservation-search';

@Injectable({
    providedIn: 'root'
})
export class ReservationService {

  private reservationBaseUri: string = this.globals.backendUri + '/reservation';

    constructor(private httpClient: HttpClient, private globals: Globals) {
    }

  /**
   * Creates a new reservation in the backend.
   * Maps to POST /api/v1/reservation
   */
  create(reservation: ReservationCreation): Observable<ReservationDetail> {
    return this.httpClient.post<ReservationDetail>(this.reservationBaseUri, reservation);
  }
  /**
   * Searches for reservations in backend as specified in the search parameters.
   *
   * @param search the searchParameters to be used in the search
   * @return an Observable of the found reservations that conforms to the given searchParameters
   */
  search(search: ReservationSearch): Observable<ReservationDetail[]> {
      let params = new HttpParams();
      if (search.customerProfileId) {
          params = params.set('customerProfileId', search.customerProfileId);
      }
      if (search.accountId) {
          params = params.set('accountId', search.accountId);
      }
      if (search.pickUpDate) {
          params = params.set('pickUpDate', search.pickUpDate);
      }
      if (search.pickUpTime) {
          params = params.set('pickUpTime', search.pickUpTime);
      }
      if (search.timePeriod) {
          params = params.set('timePeriod', search.timePeriod);
      }
      if (search.equipmentIds && search.equipmentIds.length > 0) {
          search.equipmentIds.forEach(equipmentId => {
              params = params.append('equipmentIds', equipmentId);
          })
      }
      return this.httpClient.get<ReservationDetail[]>(this.reservationBaseUri, {params});
  }

  /**
   * Updates an existing reservation in the backend.
   * Maps to PATCH /api/v1/reservation/{id}
   */
  update(id: number, reservation: any): Observable<ReservationDetail> {
    return this.httpClient.patch<ReservationDetail>(`${this.reservationBaseUri}/${id}`, reservation);
  }

}
