import {ReservationStatus} from "./reservationstatus";

export interface ReservationSearch {
  customerProfileId?: number;
  accountId?: number;
  pickUpTime?: string;
  startDate?: string;
  endDate?: string;
  searchRangeStart?: string,
  searchRangeEnd?: string,
  equipmentIds?: number[];
  reservationStatus?: ReservationStatus;
}
