//AI-assisted: Code generated with Google Gemini and adapted to fit project
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ReservationService } from './reservation.service';
import { Globals } from '../global/globals';
import { ReservationSearch } from '../dtos/reservation-search';
import { ReservationDetail } from '../dtos/reservation-detail';
import { ReservationCreation } from '../dtos/reservation-creation';
import { ReservationUpdate } from '../dtos/reservation-update';

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
        pickUpDate: '2026-03-01',
        pickUpTime: '09:00',
        rentDurationDays: 4,
        equipmentIds: [101, 102]
      };

      const mockResponse: ReservationDetail = {
        id: 1,
        customerProfileId: 1,
        accountId: 2,
        customerName: 'Max Mustermann',
        pickUpDate: '2026-03-01',
        pickUpTime: '09:00',
        returnDate: '2026-03-05',
        rentDurationDays: 4,
        confirmationEmailSent: false,
        items: []
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
        customerProfileId: 99,
        pickUpDate: '2026-03-01',
        pickUpTime: '09:00',
        rentDurationDays: 1,
        equipmentIds: []
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
        pickUpDate: '2026-04-10',
        pickUpTime: '14:00',
        returnDate: '2026-04-15',
        rentDurationDays: 5,
        confirmationEmailSent: true,
        items: []
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
        pickUpDate: '2026-05-01',
        pickUpTime: '11:00'
      };

      const mockResponse: ReservationDetail = {
        id: 55,
        customerProfileId: 4,
        accountId: 1,
        customerName: 'John Doe',
        pickUpDate: '2026-05-01',
        pickUpTime: '11:00',
        returnDate: '2026-05-05',
        rentDurationDays: 4,
        confirmationEmailSent: false,
        items: []
      };

      service.update(reservationId, mockUpdate).subscribe((data) => {
        expect(data).toBeTruthy();
        expect(data.pickUpDate).toBe('2026-05-01');
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
        pickUpDate: 'invalid-date'
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

  describe('delete', () => {
    it('should make a DELETE request with the reservation ID in the body', () => {
      const targetReservationId = 42;

      service.delete(targetReservationId).subscribe({
        next: () => {
          expect(true).toBeTrue();
        },
        error: () => {
          fail('Should not have failed');
        }
      });

      const req = httpMock.expectOne(`${globals.backendUri}/reservation`);
      expect(req.request.method).toBe('DELETE');

      expect(req.request.body).toEqual({
        id: targetReservationId,
        equipmentIds: []
      });

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

      const req = httpMock.expectOne(`${globals.backendUri}/reservation`);
      expect(req.request.method).toBe('DELETE');

      req.flush('Reservation not found', { status: 404, statusText: 'Not Found' });
    });
  });
});
