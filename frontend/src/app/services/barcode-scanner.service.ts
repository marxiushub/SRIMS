import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';
import { ReservationDetail } from '../dtos/reservation-detail';
import {ReservationUpdate} from "../dtos/reservation-update";
import {ReservationCreation} from "../dtos/reservation-creation";

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

  /**
   * Checks out Equipment that didn't belong to a Reservation for the day as part of a newly created Reservation.
   * This method is used by staff-members when doing checkout for customers who haven't made a reservation in
   * advance, to create one during the checkout-process.
   * This method is never used to check in Equipment, as in that case, a Reservation must've either already
   * existed before the checkout or otherwise must've been created during the checkout.
   */
  checkOutScanWithoutExistingReservation(reservation: ReservationCreation): Observable<ReservationDetail> {
    return this.httpClient.post<ReservationDetail>(this.reservationBaseUri, reservation);
  }
}
