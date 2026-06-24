import {ReservationStatus} from "./reservationstatus";

export interface ReservationCreationWithMode {
  customerProfileId: number;
  equipmentIds: number[];
  pickUpTime: string;
  startDate: string;
  endDate: string;
  reservationStatus: ReservationStatus;
  mode: string;
}
