import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {ReservationCreation} from '../dtos/reservation-creation';
import {ReservationDetail} from '../dtos/reservation-detail';

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
   * Updates an existing reservation in the backend.
   * Maps to PATCH /api/v1/reservation/{id}
   */
  update(id: number, reservation: any): Observable<ReservationDetail> {
    return this.httpClient.patch<ReservationDetail>(`${this.reservationBaseUri}/${id}`, reservation);
  }
}
