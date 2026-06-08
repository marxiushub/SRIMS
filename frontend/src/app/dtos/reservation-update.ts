import {ReservationStatus} from "./ReservationStatus";

export interface ReservationUpdate {
  id: number;
  customerProfileId?: number;
  pickUpTime?: string;
  startDate?: string;
  endDate?: string;
  equipmentIds?: number[];
  reservationStatus?: ReservationStatus;
}
