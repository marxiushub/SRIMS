import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CustomerService } from './customer.service';
import { Globals } from '../global/globals';
import { CustomerSearchResponse } from '../dtos/customer-search-response';
import { PasswordChange } from '../dtos/password-change';
import { UserType } from '../dtos/usertype';

describe('CustomerService', () => {
  let service: CustomerService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        CustomerService,
        { provide: Globals, useValue: { backendUri: 'http://localhost:8080/api/v1' } }
      ]
    });

    service = TestBed.inject(CustomerService);
    httpMock = TestBed.inject(HttpTestingController);
    globals = TestBed.inject(Globals);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getById', () => {
    it('should call GET with the correct URL and return the customer account', () => {
      const mockResponse: CustomerSearchResponse = {
        id: 20,
        userName: 'jdoe',
        email: 'jdoe@example.com',
        userType: UserType.CUSTOMER,
        firstName: 'John',
        lastName: 'Doe',
        dateOfBirth: '1990-01-01'
      };

      service.getById(20).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${globals.backendUri}/customers/20`);
      expect(req.request.method).toBe('GET');

      req.flush(mockResponse);
    });
  });

  describe('changePassword', () => {
    it('should call PATCH with the correct URL and body', () => {
      const passwordChange: PasswordChange = {
        oldPassword: 'oldPass1!',
        newPassword: 'NewPass1!'
      };

      const mockResponse: CustomerSearchResponse = {
        id: 20,
        userName: 'jdoe',
        email: 'jdoe@example.com',
        userType: UserType.CUSTOMER,
        firstName: 'John',
        lastName: 'Doe',
        dateOfBirth: '1990-01-01'
      };

      service.changePassword(20, passwordChange).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${globals.backendUri}/customers/password/20`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual(passwordChange);

      req.flush(mockResponse);
    });
  });
});
