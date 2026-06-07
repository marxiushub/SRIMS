import { Equipment } from './equipment';

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
}
