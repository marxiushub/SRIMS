import {UserType} from './usertype';

export interface StaffSearchResponse {
  id?: number;
  userName: string;
  email: string;
  userType: UserType;
}
