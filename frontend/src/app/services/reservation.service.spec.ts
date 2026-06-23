//AI-assisted: Code generated with Google Gemini and adapted to fit project
import {TestBed} from '@angular/core/testing';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {provideHttpClient} from '@angular/common/http';
import {ReservationService} from './reservation.service';
import {Globals} from '../global/globals';
import {ReservationSearch} from '../dtos/reservation-search';
import {ReservationDetail} from '../dtos/reservation-detail';
import {ReservationCreation} from '../dtos/reservation-creation';
import {ReservationUpdate} from '../dtos/reservation-update';
import {ReservationStatus} from "../dtos/reservationstatus";

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

  afterEach(() => {
    // Flushes missing, open HTTP-Requests after each test
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('create', () => {
    it('should make a POST request with the correct body to the base URI', () => {
      const mockCreation: ReservationCreation = {
        customerProfileId: 1,
        equipmentIds: [101, 102],
        pickUpTime: '09:00',
        startDate: '2026-03-01',
        endDate: '2026-03-02',
        reservationStatus: ReservationStatus.CREATED
      };

      const mockResponse: ReservationDetail = {
        id: 1,
        customerProfileId: 1,
        accountId: 2,
        customerName: 'Max Mustermann',
        pickUpTime: '09:00',
        startDate: '2026-03-01',
        endDate: '2026-03-02',
        confirmationEmailSent: false,
        totalPrice: 0,
        items: [],
        reservationStatus: ReservationStatus.CREATED
      };

      service.create(mockCreation).subscribe((data) => {
        expect(data).toBeTruthy();
        expect(data.id).toBe(1);
        expect(data.customerName).toBe('Max Mustermann');
      });

      const req = httpMock.expectOne(`${globals.backendUri}/reservation`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockCreation);

      req.flush(mockResponse);
    });

    it('should forward errors when creation fails', () => {
      const mockCreation: ReservationCreation = {
        equipmentIds: [],
        customerProfileId: 99,
        pickUpTime: '09:00',
        startDate: '2026-03-01',
        endDate: '2026-03-02',
        reservationStatus: ReservationStatus.CREATED
      };

      service.create(mockCreation).subscribe({
        next: () => fail('Should have failed'),
        error: (error) => {
          expect(error.status).toBe(400);
        }
      });

      const req = httpMock.expectOne(`${globals.backendUri}/reservation`);
      req.flush('Bad Request', { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('getById', () => {
    it('should make a GET request to the correct specific ID URI', () => {
      const reservationId = 12;
      const mockResponse: ReservationDetail = {
        id: 12,
        customerProfileId: 3,
        accountId: 1,
        customerName: 'Anna Smith',
        pickUpTime: '14:00',
        startDate: '2026-03-01',
        endDate: '2026-03-02',
        confirmationEmailSent: true,
        totalPrice: 0,
        items: [],
        reservationStatus: ReservationStatus.CREATED
      };

      service.getById(reservationId).subscribe((data) => {
        expect(data).toBeTruthy();
        expect(data.id).toBe(12);
        expect(data.customerName).toBe('Anna Smith');
      });

      const req = httpMock.expectOne(`${globals.backendUri}/reservation/${reservationId}`);
      expect(req.request.method).toBe('GET');

      req.flush(mockResponse);
    });

    it('should forward a 404 error if reservation ID is not found', () => {
      const invalidId = 999;

      service.getById(invalidId).subscribe({
        next: () => fail('Should have failed'),
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(`${globals.backendUri}/reservation/${invalidId}`);
      req.flush('Not Found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('update', () => {
    it('should make a PATCH request with the payload to the correct URI', () => {
      const reservationId = 55;
      const mockUpdate: ReservationUpdate = {
        id: 55,
        pickUpTime: '11:00',
        startDate: '2026-05-01'
      };

      const mockResponse: ReservationDetail = {
        id: 55,
        customerProfileId: 4,
        accountId: 1,
        customerName: 'John Doe',
        pickUpTime: '11:00',
        startDate: '2026-05-01',
        endDate: '2026-05-02',
        confirmationEmailSent: false,
        totalPrice: 0,
        items: [],
        reservationStatus: ReservationStatus.CREATED
      };

      service.update(reservationId, mockUpdate).subscribe((data) => {
        expect(data).toBeTruthy();
        expect(data.startDate).toBe('2026-05-01');
        expect(data.pickUpTime).toBe('11:00');
      });

      const req = httpMock.expectOne(`${globals.backendUri}/reservation/${reservationId}`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual(mockUpdate);

      req.flush(mockResponse);
    });

    it('should forward backend error if update fails', () => {
      const reservationId = 55;
      const mockUpdate: ReservationUpdate = {
        id: 55,
        startDate: 'invalid-date'
      };

      service.update(reservationId, mockUpdate).subscribe({
        next: () => fail('Should have failed'),
        error: (error) => {
          expect(error.status).toBe(422);
        }
      });

      const req = httpMock.expectOne(`${globals.backendUri}/reservation/${reservationId}`);
      req.flush('Unprocessable Entity', { status: 422, statusText: 'Unprocessable Entity' });
    });
  });

  describe('search', () => {
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
        pickUpTime: '10:00',
        startDate: '2026-12-24',
        endDate: '2026-12-25',
        searchRangeStart: '2026-12-20',
        searchRangeEnd: '2026-12-30',
        reservationStatus: ReservationStatus.CREATED
      };

      service.search(mockSearch).subscribe();

      const req = httpMock.expectOne((request) => request.url === `${globals.backendUri}/reservation`);
      expect(req.request.method).toBe('GET');

      expect(req.request.params.get('customerProfileId')).toBe('123');
      expect(req.request.params.get('accountId')).toBe('456');
      expect(req.request.params.get('pickUpTime')).toBe('10:00');
      expect(req.request.params.get('startDate')).toBe('2026-12-24');
      expect(req.request.params.get('endDate')).toBe('2026-12-25');
      expect(req.request.params.get('searchRangeStart')).toBe('2026-12-20');
      expect(req.request.params.get('searchRangeEnd')).toBe('2026-12-30');
      expect(req.request.params.get('reservationStatus')).toBe(ReservationStatus.CREATED);

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
          startDate: '2026-02-15',
          endDate: '2026-02-20',
          confirmationEmailSent: true,
          totalPrice: 0,
          items: [],
          reservationStatus: ReservationStatus.CREATED
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

  describe('delete', () => {
    it('should make a DELETE request with the reservation ID in the URL path', () => {
      const targetReservationId = 42;

      service.delete(targetReservationId).subscribe({
        next: () => {
          expect(true).toBeTrue();
        },
        error: () => {
          fail('Should not have failed');
        }
      });

      const req = httpMock.expectOne(`${globals.backendUri}/reservation/${targetReservationId}`);
      expect(req.request.method).toBe('DELETE');
      expect(req.request.body).toBeNull();

      req.flush(null);
    });

    it('should forward backend errors correctly when deleting a reservation fails', () => {
      const targetReservationId = 999;

      service.delete(targetReservationId).subscribe({
        next: () => {
          fail('Should have failed with a 404 error');
        },
        error: (error) => {
          expect(error).toBeTruthy();
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(`${globals.backendUri}/reservation/${targetReservationId}`);
      expect(req.request.method).toBe('DELETE');

      req.flush('Reservation not found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('updateForStaff', () => {
    it('should make a PATCH request to the staff URI with the payload', () => {
      const reservationId = 77;
      const mockUpdate: ReservationUpdate = {
        id: 77,
        pickUpTime: '16:00'
      };

      const mockResponse: ReservationDetail = {
        id: 77,
        customerProfileId: 10,
        accountId: 2,
        customerName: 'Staff Modified',
        pickUpTime: '16:00',
        startDate: '2026-06-01',
        endDate: '2026-06-05',
        confirmationEmailSent: true,
        items: [],
        reservationStatus: ReservationStatus.CREATED
      };

      service.updateForStaff(reservationId, mockUpdate).subscribe((data) => {
        expect(data).toBeTruthy();
        expect(data.pickUpTime).toBe('16:00');
      });

      const req = httpMock.expectOne(`${globals.backendUri}/reservation/staff/${reservationId}`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual(mockUpdate);

      req.flush(mockResponse);
    });
  });

  describe('deleteForStaff', () => {
    it('should make a DELETE request to the staff URI with the reservation ID in the URL path', () => {
      const targetReservationId = 101;

      service.deleteForStaff(targetReservationId).subscribe({
        next: () => expect(true).toBeTrue(),
        error: () => fail('Should not have failed')
      });

      const req = httpMock.expectOne(`${globals.backendUri}/reservation/staff/${targetReservationId}`);
      expect(req.request.method).toBe('DELETE');
      expect(req.request.body).toBeNull();

      req.flush(null);
    });
  });
});
