export interface ReservationCreation {
  customerProfileId: number;
  equipmentIds: number[];
  pickUpTime: string;
  pickUpDate: string;
  rentDurationDays: number;
}
