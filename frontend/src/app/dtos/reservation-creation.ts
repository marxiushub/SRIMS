import {ReservationStatus} from "./ReservationStatus";

export interface ReservationCreation {
  customerProfileId: number;
  equipmentIds: number[];
  pickUpTime: string;
  startDate: string;
  endDate: string;
  reservationStatus: ReservationStatus;
}
