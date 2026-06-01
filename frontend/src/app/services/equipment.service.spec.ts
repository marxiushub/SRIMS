//AI-assisted: Code generated with Google Gemini and adapted to fit project
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { EquipmentService } from './equipment.service';
import { Globals } from '../global/globals';
import { RentalStatus } from '../dtos/rentalstatus';
import { SkillLevel } from '../dtos/skilllevel';
import { EquipmentType } from "../dtos/equipmenttype";
import { Equipment } from '../dtos/equipment';
import { EquipmentCreation } from '../dtos/equipment-creation';
import { EquipmentUpdate } from '../dtos/equipment-update';
import { EquipmentSearch } from '../dtos/equipment-search';

describe('EquipmentService', () => {
  let service: EquipmentService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        EquipmentService,
        Globals,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(EquipmentService);
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

  describe('getAll', () => {
    it('should send a GET request to the backend URI', () => {
      const mockBackendData: Equipment[] = [
        {
          id: 99,
          barcodeId: 'test-barcode-123',
          model: 'Model X',
          price: 20.0,
          status: RentalStatus.FREE,
          targetSkillLevel: SkillLevel.BEGINNER,
          equipmentType: EquipmentType.SKI
        }
      ];

      service.getAll().subscribe((data) => {
        expect(data).toEqual(mockBackendData);
      });

      const req = httpMock.expectOne(`${globals.backendUri}/equipment`);
      expect(req.request.method).toBe('GET');

      req.flush(mockBackendData);
    });
  });

  describe('getById', () => {
    it('should send a GET request to the specific ID path', () => {
      const equipmentId = 42;
      const mockEquipment: Equipment = {
        id: equipmentId,
        barcodeId: 'barcode-42',
        model: 'Carving Pro',
        price: 25.5,
        status: RentalStatus.FREE,
        targetSkillLevel: SkillLevel.ADVANCED,
        equipmentType: EquipmentType.SKI
      };

      service.getById(equipmentId).subscribe((data) => {
        expect(data).toEqual(mockEquipment);
      });

      const req = httpMock.expectOne(`${globals.backendUri}/equipment/${equipmentId}`);
      expect(req.request.method).toBe('GET');

      req.flush(mockEquipment);
    });

    it('should forward errors when equipment ID is not found', () => {
      const invalidId = 999;

      service.getById(invalidId).subscribe({
        next: () => fail('Should have failed'),
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(`${globals.backendUri}/equipment/${invalidId}`);
      req.flush('Not Found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('getByBarcodeId', () => {
    it('should send a GET request to the barcode path with the string id', () => {
      const targetBarcode = 'BC-999-XYZ';
      const mockEquipment: Equipment = {
        id: 101,
        barcodeId: targetBarcode,
        model: 'Snow-Master',
        price: 30.0,
        status: RentalStatus.FREE,
        targetSkillLevel: SkillLevel.ADVANCED,
        equipmentType: EquipmentType.SNOWBOARD
      };

      service.getByBarcodeId(targetBarcode).subscribe((data) => {
        expect(data).toEqual(mockEquipment);
      });

      const req = httpMock.expectOne(`${globals.backendUri}/equipment/barcode/${targetBarcode}`);
      expect(req.request.method).toBe('GET');

      req.flush(mockEquipment);
    });
  });

  describe('delete', () => {
    it('should send a DELETE request to the ID path', () => {
      const equipmentId = 77;

      service.delete(equipmentId).subscribe();

      const req = httpMock.expectOne(`${globals.backendUri}/equipment/${equipmentId}`);
      expect(req.request.method).toBe('DELETE');

      req.flush(null);
    });
  });

  describe('create', () => {
    it('should send a POST request with the creation payload', () => {
      const mockCreation: EquipmentCreation = {
        model: 'New Ski Model 2026',
        price: 35.0,
        targetSkillLevel: SkillLevel.BEGINNER,
        type: EquipmentType.SKI,
        status: RentalStatus.FREE,
        creationNumber: 10001
      };

      const mockResponse: Equipment = {
        id: 202,
        barcodeId: 'generated-barcode-202',
        model: 'New Ski Model 2026',
        price: 35.0,
        status: RentalStatus.FREE,
        targetSkillLevel: SkillLevel.BEGINNER,
        equipmentType: EquipmentType.SKI
      };

      service.create(mockCreation).subscribe((data) => {
        expect(data).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${globals.backendUri}/equipment`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockCreation);

      req.flush(mockResponse);
    });
  });

  describe('update', () => {
    it('should send a PATCH request with the updated data to the correct ID path', () => {
      const equipmentId = 5;

      const mockUpdate: EquipmentUpdate = {
        type: EquipmentType.SKI,
        model: 'Old Model Name',
        targetSkillLevel: SkillLevel.ADVANCED,
        length: 170,
        price: 45.0,
        status: RentalStatus.RENTED,
        size: 42,
        soleLengthMm: 310,
        lacingSystem: 'BOA'
      };

      const mockResponse: Equipment = {
        id: equipmentId,
        barcodeId: 'existing-barcode',
        model: 'Old Model Name',
        price: 45.0,
        status: RentalStatus.RENTED,
        targetSkillLevel: SkillLevel.ADVANCED,
        equipmentType: EquipmentType.SKI
      };

      service.update(equipmentId, mockUpdate).subscribe((data) => {
        expect(data).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${globals.backendUri}/equipment/${equipmentId}`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual(mockUpdate);

      req.flush(mockResponse);
    });
  });

  describe('search', () => {
    it('should append all existing search filters as query parameters', () => {
      const mockSearch: EquipmentSearch = {
        model: 'Fischer',
        type: EquipmentType.SKI,
        status: RentalStatus.FREE,
        targetSkillLevel: SkillLevel.ADVANCED,
        start: '2026-06-01',
        end: '2026-06-10'
      };

      service.search(mockSearch).subscribe();

      const req = httpMock.expectOne((request) => request.url === `${globals.backendUri}/equipment`);
      expect(req.request.method).toBe('GET');

      expect(req.request.params.get('model')).toBe('Fischer');
      expect(req.request.params.get('type')).toBe(EquipmentType.SKI);
      expect(req.request.params.get('status')).toBe(RentalStatus.FREE);
      expect(req.request.params.get('targetSkillLevel')).toBe(SkillLevel.ADVANCED);
      expect(req.request.params.get('start')).toBe('2026-06-01');
      expect(req.request.params.get('end')).toBe('2026-06-10');

      req.flush([]);
    });

    it('should not append parameters if the search fields are empty/undefined', () => {
      const mockSearch: EquipmentSearch = {};

      service.search(mockSearch).subscribe();

      const req = httpMock.expectOne((request) => request.url === `${globals.backendUri}/equipment`);
      expect(req.request.method).toBe('GET');

      expect(req.request.params.keys().length).toBe(0);

      req.flush([]);
    });
  });
});
