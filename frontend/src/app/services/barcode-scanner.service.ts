import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';
import { ReservationDetail } from '../dtos/reservation-detail';
import {ReservationUpdate} from "../dtos/reservation-update";

@Injectable({
  providedIn: 'root'
})
export class BarcodeScannerService {

  private reservationBaseUri: string = this.globals.backendUri + '/scanner';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Checks out or checks in a Reservation along with its equipment for an already existing,
   * previously created Reservation.
   * This method is used by staff-members when doing the checkout for customers who made a Reservation in advance,
   * or when doing the check-in for any Reservation later.
   * Maps to PATCH /api/v1/scanner/{id}
   */
  checkOutOrInScanWithExistingReservation(reservation: ReservationUpdate): Observable<ReservationDetail> {
    return this.httpClient.patch<ReservationDetail>(this.reservationBaseUri + `/${reservation.id}`, reservation);
  }
}
