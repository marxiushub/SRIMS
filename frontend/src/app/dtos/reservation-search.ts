export interface ReservationSearch {
  customerProfileId?: number;
  accountId?: number;
  pickUpDate?: string;
  pickUpTime?: string;
  timePeriod?: string;
  equipmentIds?: number[];
}
