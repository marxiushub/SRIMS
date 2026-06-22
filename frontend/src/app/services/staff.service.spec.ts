//AI-assisted: Code generated with Google Gemini and adapted to fit project
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { StaffService } from './staff.service';
import { Globals } from '../global/globals';
import { CustomerSearch } from '../dtos/customer-search';
import { CustomerSearchResponse } from '../dtos/customer-search-response';
import { UserType } from '../dtos/usertype';

describe('StaffService', () => {
  let service: StaffService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        StaffService,
        { provide: Globals, useValue: { backendUri: 'http://localhost:8080/api/v1' } }
      ]
    });

    service = TestBed.inject(StaffService);
    httpMock = TestBed.inject(HttpTestingController);
    globals = TestBed.inject(Globals);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('searchCustomers', () => {
    it('should call GET with correct URL and map all search parameters correctly', () => {
      const searchCriteria: CustomerSearch = {
        firstName: 'Max',
        lastName: 'Mustermann',
        userName: 'max123',
        email: 'max@example.com'
      };

      const mockResponse: CustomerSearchResponse[] = [
        {
          id: 42,
          userName: 'max123',
          email: 'max@example.com',
          userType: UserType.CUSTOMER,
          firstName: 'Max',
          lastName: 'Mustermann',
          dateOfBirth: '1990-01-01'
        }
      ];

      service.searchCustomers(searchCriteria).subscribe((response) => {
        expect(response).toBeTruthy();
        expect(response.length).toBe(1);
        expect(response[0].id).toBe(42);
        expect(response[0].userType).toBe(UserType.CUSTOMER);
      });

      const req = httpMock.expectOne((request) => request.url === `${globals.backendUri}/staff/customers/search`);
      expect(req.request.method).toBe('GET');

      expect(req.request.params.get('firstName')).toBe('Max');
      expect(req.request.params.get('lastName')).toBe('Mustermann');
      expect(req.request.params.get('userName')).toBe('max123');
      expect(req.request.params.get('email')).toBe('max@example.com');

      req.flush(mockResponse);
    });

    it('should call GET with no parameters if search criteria are empty', () => {
      const emptyCriteria: CustomerSearch = {};

      service.searchCustomers(emptyCriteria).subscribe((response) => {
        expect(response).toEqual([]);
      });

      const expectedUrl = `${globals.backendUri}/staff/customers/search`;
      const req = httpMock.expectOne(expectedUrl);
      expect(req.request.method).toBe('GET');
      expect(req.request.params.keys().length).toBe(0);

      req.flush([]);
    });
  });
});
