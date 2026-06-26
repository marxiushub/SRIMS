import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {CustomerSearchResponse} from '../dtos/customer-search-response';
import {PasswordChange} from '../dtos/password-change';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  private customerBaseUri: string = this.globals.backendUri + '/customers';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Loads the account data of the customer with the given id from the backend.
   *
   * @param id the id of the customer to load
   */
  getById(id: number): Observable<CustomerSearchResponse> {
    return this.httpClient.get<CustomerSearchResponse>(`${this.customerBaseUri}/${id}`);
  }

  /**
   * Changes the password of the customer with the given id.
   *
   * @param id the id of the customer whose password should be changed
   * @param passwordChange the DTO containing the old and the new password
   */
  changePassword(id: number, passwordChange: PasswordChange): Observable<CustomerSearchResponse> {
    return this.httpClient.patch<CustomerSearchResponse>(`${this.customerBaseUri}/password/${id}`, passwordChange);
  }
}
