import { Equipment } from './equipment';

export interface ReservationDetail {
  id: number;
  customerProfileId: number;
  accountId: number;
  customerName: string;
  pickUpTime: string;
  pickUpDate: string;
  returnDate: string;
  rentDurationDays: number;
  confirmationEmailSent: boolean;
  items: Equipment[];
}
