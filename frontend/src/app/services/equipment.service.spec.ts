import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { EquipmentService } from './equipment.service';
import { Globals } from '../global/globals';
import { RentalStatus } from '../dtos/rentalstatus';
import { SkillLevel } from '../dtos/skilllevel';
import {EquipmentType} from "../dtos/equipmenttype";

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
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAll', () => {
    it('should send a GET request to the backend URI', () => {
      const mockBackendData = [
        { id: 99,
          barcodeId: 'test-barcode-123',
          model: 'Model X',
          price: 20.0,
          status: RentalStatus.FREE,
          targetSkillLevel: SkillLevel.BEGINNER,
          equipmentType: EquipmentType.SKI }
      ];

      service.getAll().subscribe((data) => {
        expect(data).toEqual(mockBackendData);
      });

      const req = httpMock.expectOne(`${globals.backendUri}/equipment`);
      expect(req.request.method).toBe('GET');

      req.flush(mockBackendData);
    });
  });
});
