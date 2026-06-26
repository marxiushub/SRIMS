import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {BarcodeScannerService} from './barcode-scanner.service';
import {Globals} from '../global/globals';
import {ReservationDetail} from '../dtos/reservation-detail';
import {ReservationUpdate} from '../dtos/reservation-update';
import {ReservationCreationWithMode} from '../dtos/reservation-creation-with-mode';

describe('BarcodeScannerService', () => {
  let service: BarcodeScannerService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BarcodeScannerService, Globals],
    });

    service = TestBed.inject(BarcodeScannerService);
    httpMock = TestBed.inject(HttpTestingController);
    globals = TestBed.inject(Globals);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('checkOutOrInScanWithExistingReservation', () => {
    it('should send a PATCH request to the correct URL', () => {
      const reservation: ReservationUpdate = {id: 42} as unknown as ReservationUpdate;
      const mockResponse: ReservationDetail = {id: 42} as unknown as ReservationDetail;

      service.checkOutOrInScanWithExistingReservation(reservation).subscribe(res => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${globals.backendUri}/scanner/42`);
      expect(req.request.method).toBe('PATCH');
      req.flush(mockResponse);
    });

    it('should include the full reservation payload in the request body', () => {
      const reservation: ReservationUpdate = {
        id: 7,
        status: 'CHECKED_OUT',
        equipmentIds: [1, 2, 3],
      } as unknown as ReservationUpdate;

      service.checkOutOrInScanWithExistingReservation(reservation).subscribe();

      const req = httpMock.expectOne(`${globals.backendUri}/scanner/7`);
      expect(req.request.body).toEqual(reservation);
      req.flush({} as unknown as ReservationDetail);
    });

    it('should return the ReservationDetail emitted by the server', () => {
      const reservation: ReservationUpdate = {id: 1} as unknown as ReservationUpdate;
      const mockDetail: ReservationDetail = {
        id: 1,
        status: 'CHECKED_OUT',
        customerName: 'Jane Doe',
      } as unknown as ReservationDetail;

      let result: ReservationDetail | undefined;
      service.checkOutOrInScanWithExistingReservation(reservation).subscribe(res => {
        result = res;
      });

      const req = httpMock.expectOne(`${globals.backendUri}/scanner/1`);
      req.flush(mockDetail);

      expect(result).toEqual(mockDetail);
    });

    it('should propagate HTTP errors to the caller', () => {
      const reservation: ReservationUpdate = {id: 99} as unknown as ReservationUpdate;
      let errorReceived = false;

      service.checkOutOrInScanWithExistingReservation(reservation).subscribe({
        next: () => fail('expected an error, not a value'),
        error: () => (errorReceived = true),
      });

      const req = httpMock.expectOne(`${globals.backendUri}/scanner/99`);
      req.flush('Not found', {status: 404, statusText: 'Not Found'});

      expect(errorReceived).toBeTrue();
    });
  });

  describe('checkOutScanWithoutExistingReservation', () => {
    it('should send a POST request to the base scanner URL', () => {
      const reservation: ReservationCreationWithMode = {
        mode: 'CHECKOUT',
        equipmentIds: [10, 11],
      } as unknown as ReservationCreationWithMode;
      const mockResponse: ReservationDetail = {id: 100} as unknown as ReservationDetail;

      service.checkOutScanWithoutExistingReservation(reservation).subscribe(res => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${globals.backendUri}/scanner`);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should include the full reservation payload in the request body', () => {
      const reservation: ReservationCreationWithMode = {
        mode: 'MAINTENANCE',
        equipmentIds: [5],
        customerId: 3,
      } as unknown as ReservationCreationWithMode;

      service.checkOutScanWithoutExistingReservation(reservation).subscribe();

      const req = httpMock.expectOne(`${globals.backendUri}/scanner`);
      expect(req.request.body).toEqual(reservation);
      req.flush({} as unknown as ReservationDetail);
    });

    it('should return the ReservationDetail emitted by the server', () => {
      const reservation: ReservationCreationWithMode = {
        mode: 'CHECKOUT',
        equipmentIds: [7],
      } as unknown as ReservationCreationWithMode;
      const mockDetail: ReservationDetail = {
        id: 55,
        status: 'CHECKED_OUT',
        customerName: 'John Smith',
      } as unknown as ReservationDetail;

      let result: ReservationDetail | undefined;
      service.checkOutScanWithoutExistingReservation(reservation).subscribe(res => {
        result = res;
      });

      const req = httpMock.expectOne(`${globals.backendUri}/scanner`);
      req.flush(mockDetail);

      expect(result).toEqual(mockDetail);
    });

    it('should propagate HTTP errors to the caller', () => {
      const reservation: ReservationCreationWithMode = {
        mode: 'CHECKOUT',
        equipmentIds: [],
      } as unknown as ReservationCreationWithMode;
      let errorReceived = false;

      service.checkOutScanWithoutExistingReservation(reservation).subscribe({
        next: () => fail('expected an error, not a value'),
        error: () => (errorReceived = true),
      });

      const req = httpMock.expectOne(`${globals.backendUri}/scanner`);
      req.flush('Bad Request', {status: 400, statusText: 'Bad Request'});

      expect(errorReceived).toBeTrue();
    });

    it('should correctly use mode MAINTENANCE to distinguish a maintenance checkout', () => {
      const maintenanceReservation: ReservationCreationWithMode = {
        mode: 'MAINTENANCE',
        equipmentIds: [20, 21],
      } as unknown as ReservationCreationWithMode;

      service.checkOutScanWithoutExistingReservation(maintenanceReservation).subscribe();

      const req = httpMock.expectOne(`${globals.backendUri}/scanner`);
      expect(req.request.body.mode).toBe('MAINTENANCE');
      req.flush({} as unknown as ReservationDetail);
    });
  });
});
