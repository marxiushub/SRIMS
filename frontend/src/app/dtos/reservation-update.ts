export interface ReservationUpdate {
  id: number;
  pickUpTime?: string;
  pickUpDate?: string;
  rentDurationDays?: number;
  equipmentIds?: number[];
  customerProfileId?: number; 
}
