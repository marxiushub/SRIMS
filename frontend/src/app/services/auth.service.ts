import {Injectable} from '@angular/core';
import {AuthRequest} from '../dtos/auth-request';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs/operators';
import {jwtDecode} from 'jwt-decode';
import {Globals} from '../global/globals';
import {CustomerCreationDto} from '../dtos/customer-creation';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private authBaseUri: string = this.globals.backendUri + '/authentication';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Login in the user. If it was successful, a valid JWT token will be stored
   *
   * @param authRequest User data
   */
  loginUser(authRequest: AuthRequest): Observable<string> {
    return this.httpClient.post(this.authBaseUri, authRequest, {responseType: 'text'})
      .pipe(
        tap((authResponse: string) => this.setToken(authResponse))
      );
  }

  /**
   * Register a new customer account.
   *
   * @param customerDto User data for the new account
   */
  registerUser(customerDto: CustomerCreationDto): Observable<any> {
    return this.httpClient.post<any>(`${this.authBaseUri.replace('/authentication', '')}/customer/create`, customerDto);
  }

  /**
   * Request a password reset for the given email. The backend generates a new
   * password and sends it to that email address; no request body is needed.
   *
   * @param email the email of the customer who forgot their password
   */
  resetPassword(email: string): Observable<any> {
    return this.httpClient.patch<any>(
      `${this.authBaseUri.replace('/authentication', '')}/customer/password-resets/${encodeURIComponent(email)}`,
      null
    );
  }

  /**
   * Check if a valid JWT token is saved in the localStorage
   */
  isLoggedIn() {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    const expirationDate = this.getTokenExpirationDate(token);
    return !!expirationDate && (expirationDate.valueOf() > new Date().valueOf());
  }

  logoutUser() {
    console.log('Logout');
    localStorage.removeItem('authToken');
  }

  getToken() {
    return localStorage.getItem('authToken');
  }

  /**
   * Returns the user role based on the current token
   */
  getUserRole() {
    const token = this.getToken();
    if (token != null) {
      const decoded: any = jwtDecode(token);
      const permissions: string[] = decoded.perms || [];
      if (permissions?.includes('STAFF_READ')) {
        return 'ADMIN';
      }
      if (permissions.length > 0){
        return 'USER';
      }
    }
    return 'UNDEFINED';
  }

  /**
   * Returns the id of the currently logged-in user, decoded from the JWT's 'uid' claim,
   * or null if no token is present.
   */
  getUserId(): number | null {
    const token = this.getToken();
    if (token != null) {
      const decoded: any = jwtDecode(token);
      return decoded.uid ?? null;
    }
    return null;
  }

  private setToken(authResponse: string) {
    localStorage.setItem('authToken', authResponse);
  }

  private getTokenExpirationDate(token: string): Date | null {

    const decoded: any = jwtDecode(token);
    if (decoded.exp === undefined) {
      return null;
    }

    const date = new Date(0);
    date.setUTCSeconds(decoded.exp);
    return date;
  }

}
