import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';
import { CustomerSearch } from '../dtos/customer-search';
import { CustomerSearchResponse } from '../dtos/customer-search-response';
import { StaffSearchResponse } from '../dtos/staff-search-response';
import { PasswordChange } from '../dtos/password-change';
import {StaffCreationDto} from "../dtos/staff-creation";

@Injectable({
  providedIn: 'root'
})
export class StaffService {

  private staffBaseUri: string = this.globals.backendUri + '/staff';
  private staffCustomersBaseUri: string = this.globals.backendUri + '/staff/customers';

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  /**
   * Searches for customer accounts based on dynamic query parameters.
   * Maps to GET /api/v1/staff/customers/search
   * @param search the search criteria containing optional firstName, lastName, userName or email
   * @return an Observable of matching CustomerSearchResponse objects
   */
  searchCustomers(search: CustomerSearch): Observable<CustomerSearchResponse[]> {
    let params = new HttpParams();

    if (search.firstName) {
      params = params.set('firstName', search.firstName);
    }
    if (search.lastName) {
      params = params.set('lastName', search.lastName);
    }
    if (search.userName) {
      params = params.set('userName', search.userName);
    }
    if (search.email) {
      params = params.set('email', search.email);
    }

    return this.httpClient.get<CustomerSearchResponse[]>(`${this.staffCustomersBaseUri}/search`, { params });
  }

  /**
   * Loads the account data of the staff member with the given id from the backend.
   * Maps to GET /api/v1/staff/{id}
   *
   * @param id the id of the staff account to load
   */
  getById(id: number): Observable<StaffSearchResponse> {
    return this.httpClient.get<StaffSearchResponse>(`${this.staffBaseUri}/${id}`);
  }

  /**
   * Changes the password of the staff member with the given id.
   * Maps to PATCH /api/v1/staff/password/{id}
   *
   * @param id the id of the staff account whose password should be changed
   * @param passwordChange the DTO containing the old and the new password
   */
  changePassword(id: number, passwordChange: PasswordChange): Observable<StaffSearchResponse> {
    return this.httpClient.patch<StaffSearchResponse>(`${this.staffBaseUri}/password/${id}`, passwordChange);
  }

  /**
   * Requests a password reset for the staff account with the given email.
   * Maps to PATCH /api/v1/staff/password-resets/{email}
   * Requires the caller to be an authenticated staff member with STAFF_UPDATE authority.
   * The backend generates a new password and emails it to that address; no request body is needed.
   *
   * @param email the email of the staff account whose password should be reset
   */
  resetPasswordForStaff(email: string): Observable<any> {
    return this.httpClient.patch<any>(`${this.staffBaseUri}/password-resets/${encodeURIComponent(email)}`, null);
  }

  /**
   * Creates a new staff account.
   * Maps to POST /api/v1/staff/create
   * Requires the caller to be an authenticated staff member with STAFF_CREATE authority.
   *
   * @param staffCreationDto the DTO containing the new staff account's userName, password, and email
   */
  createStaff(staffCreationDto: StaffCreationDto): Observable<any> {
    return this.httpClient.post<any>(`${this.staffBaseUri}/create`, staffCreationDto);
  }
}
