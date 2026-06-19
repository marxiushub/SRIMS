import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from "../global/globals";
import {CustomerProfile} from '../dtos/customer-profile';
import {CustomerProfileCreationUpdate} from '../dtos/customer-profile-creation-update';

@Injectable({
  providedIn: 'root'
})
export class CustomerProfileService {

  private customerBaseUri: string = this.globals.backendUri + '/customer/profiles';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Loads all profiles from the backend corresponding to the given customer ID.
   */
  getCustomerProfiles(): Observable<CustomerProfile[]> {
    return this.httpClient.get<CustomerProfile[]>(this.customerBaseUri);
  }

  /**
   * Loads profile by id from the backend.
   */
  getById(id: number): Observable<CustomerProfile> {
    return this.httpClient.get<CustomerProfile>(`${this.customerBaseUri}/${id}`);
  }

  /**
   * Deletes profile by id in backend.
   */
  delete(id: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.customerBaseUri}/${id}`);
  }

  /**
   * Creates new profile in backend.
   */
  create(profile: CustomerProfileCreationUpdate): Observable<CustomerProfile> {
    return this.httpClient.post<CustomerProfile>(this.customerBaseUri, profile);
  }

  /**
   * Updates profile in backend.
   */
  update(id: number, profile: CustomerProfileCreationUpdate): Observable<CustomerProfile> {
    return this.httpClient.patch<CustomerProfile>(`${this.customerBaseUri}/${id}`, profile);

  }
}
