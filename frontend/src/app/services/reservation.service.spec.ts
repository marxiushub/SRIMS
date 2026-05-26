import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ReservationService } from './reservation.service';
import { Globals } from '../global/globals';
import { ReservationSearch } from '../dtos/reservation-search';
import { ReservationDetail } from '../dtos/reservation-detail';

//AI-assisted: Code generated with Google Gemini and adapted
describe('ReservationService', () => {
  let service: ReservationService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ReservationService,
        {
          provide: Globals,
          useValue: { backendUri: 'http://localhost:8080/api/v1' }
        }
      ]
    });

    service = TestBed.inject(ReservationService);
    httpMock = TestBed.inject(HttpTestingController);
    globals = TestBed.inject(Globals);
  });

  //Flushes missing, open HTTP-Requests after each test
  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should make a GET request to the correct URI without query parameters when search object is empty', () => {
    const mockSearch: ReservationSearch = {};

    service.search(mockSearch).subscribe((reservations) => {
      expect(reservations).toBeTruthy();
      expect(reservations.length).toBe(0);
    });

    const req = httpMock.expectOne(`${globals.backendUri}/reservation`);
    expect(req.request.method).toBe('GET');

    req.flush([]);
  });

  it('should correctly map simple search fields into URL query parameters', () => {
    const mockSearch: ReservationSearch = {
      customerProfileId: 123,
      accountId: 456,
      pickUpDate: '2026-12-24',
      pickUpTime: '10:00',
      timePeriod: 'MORNING'
    };

    service.search(mockSearch).subscribe();

    const req = httpMock.expectOne((request) => request.url === `${globals.backendUri}/reservation`);
    expect(req.request.method).toBe('GET');

    expect(req.request.params.get('customerProfileId')).toBe('123');
    expect(req.request.params.get('accountId')).toBe('456');
    expect(req.request.params.get('pickUpDate')).toBe('2026-12-24');
    expect(req.request.params.get('pickUpTime')).toBe('10:00');
    expect(req.request.params.get('timePeriod')).toBe('MORNING');

    req.flush([]);
  });

  it('should correctly append multiple equipmentIds as repeated query parameters for Spring Boot', () => {
    const mockSearch: ReservationSearch = {
      equipmentIds: [10, 20, 30]
    };

    service.search(mockSearch).subscribe();

    const req = httpMock.expectOne((request) => request.url === `${globals.backendUri}/reservation`);
    expect(req.request.method).toBe('GET');

    const paramValues = req.request.params.getAll('equipmentIds');
    expect(paramValues).toBeTruthy();
    expect(paramValues?.length).toBe(3);
    expect(paramValues).toEqual(['10', '20', '30']);

    req.flush([]);
  });

  it('should return the reservation array emitted by the backend', () => {
    const mockSearch: ReservationSearch = { equipmentIds: [1] };
    const mockResponse: ReservationDetail[] = [
      {
        id: 99,
        customerProfileId: 5,
        accountId: 1,
        customerName: 'John Doe',
        pickUpTime: '08:30',
        pickUpDate: '2026-02-15',
        returnDate: '2026-02-20',
        rentDurationDays: 5,
        confirmationEmailSent: true,
        items: []
      }
    ];

    service.search(mockSearch).subscribe((data) => {
      expect(data.length).toBe(1);
      expect(data[0].id).toBe(99);
      expect(data[0].customerName).toBe('John Doe');
    });

    const req = httpMock.expectOne((request) => request.url === `${globals.backendUri}/reservation`);

    req.flush(mockResponse);
  });
});
