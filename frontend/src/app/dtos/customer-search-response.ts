import { UserType } from "./usertype";

export interface CustomerSearchResponse {
  id?: number;
  userName: string;
  email: string;
  userType: UserType;
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
}
