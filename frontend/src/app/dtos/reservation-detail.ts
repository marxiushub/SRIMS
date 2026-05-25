export interface ReservationDetail {
  customerProfileId: number;
  equipmentIds: number[];
  pickUpTime: string;
  pickUpDate: string;
  rentDurationDays: number;
}
