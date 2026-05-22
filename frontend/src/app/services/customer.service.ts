import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from "../global/globals";
import {CustomerProfile} from '../dtos/customer-profile';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  private customerBaseUri: string = this.globals.backendUri + '/customer';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  //TODO update method once backend endpoint exists
  /**
   * Loads all profiles from the backend
   */
  getAll(): Observable<CustomerProfile[]> {
    return this.httpClient.get<CustomerProfile[]>(this.customerBaseUri);
  }
}
