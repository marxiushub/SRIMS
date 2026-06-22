import { Equipment } from './equipment';
import {ReservationStatus} from "./reservationstatus";

export interface ReservationDetail {
  id: number;
  customerProfileId: number;
  accountId: number;
  customerName: string;
  pickUpTime: string;
  startDate: string,
  endDate: string;
  confirmationEmailSent: boolean;
  items: Equipment[];
  reservationStatus: ReservationStatus;
}
