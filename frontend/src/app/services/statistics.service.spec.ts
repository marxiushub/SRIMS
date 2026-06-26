import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { Globals } from '../global/globals';


import { StatisticsService } from './statistics.service';
import { StatisticsRequestDto } from '../dtos/statistics-request';
import { StatisticsResponseDto } from '../dtos/statistics-response';



describe('StatisticsService', () => {
  let service: StatisticsService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        StatisticsService,
        {
          provide: Globals,
          useValue: { backendUri: 'http://localhost:8080/api/v1' }
        }
      ]
    });

    service = TestBed.inject(StatisticsService);
    httpMock = TestBed.inject(HttpTestingController);
    globals = TestBed.inject(Globals);
  });

  afterEach(() => {

    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getEquipmentStatistics', () => {

    it('should make a POST request with the correct payload and return itemCounts if detailDegree is true', () => {

      const mockRequest: StatisticsRequestDto = {
        searchStart: '2026-01-01',
        searchEnd: '2026-12-31',
        detailDegree: true

      };

      const mockResponse: StatisticsResponseDto = {
        detailDegree: true,
        itemCounts: {
          'EQ-1001': 45,
          'EQ-1002': 12
        }
      };


      service.getEquipmentStatistics(mockRequest).subscribe((data) => {
        expect(data).toBeTruthy();
        expect(data.detailDegree).toBeTrue();
        expect(data.itemCounts).toBeDefined();
        expect(data.itemCounts?.['EQ-1001']).toBe(45);
      });


      const req = httpMock.expectOne(`${globals.backendUri}/statistics`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockRequest);


      req.flush(mockResponse);
    });

    it('should handle modelCounts correctly when detailDegree is false', () => {

      const mockRequest: StatisticsRequestDto = {
        searchStart: '2026-05-01',
        searchEnd: '2026-05-31',
        detailDegree: false
      };

      const mockResponse: StatisticsResponseDto = {
        detailDegree: false,
        modelCounts: {
          'Atomic Redster': 120,
          'Fischer RC4': 85
        }
      };

      service.getEquipmentStatistics(mockRequest).subscribe((data) => {
        expect(data.detailDegree).toBeFalse();
        expect(data.modelCounts).toBeDefined();
        expect(data.modelCounts?.['Atomic Redster']).toBe(120);
      });

      const req = httpMock.expectOne(`${globals.backendUri}/statistics`);
      req.flush(mockResponse);
    });

    it('should forward backend error if the statistics request fails', () => {
      // 1. Mock-Daten vorbereiten
      const mockRequest: StatisticsRequestDto = {
        searchStart: 'invalid-date',
        searchEnd: '2026-12-31',
        detailDegree: true
      };

      service.getEquipmentStatistics(mockRequest).subscribe({
        next: () => fail('Should have failed'),
        error: (error) => {
          expect(error).toBeTruthy();
          expect(error.status).toBe(400);
        }
      });


      const req = httpMock.expectOne(`${globals.backendUri}/statistics`);
      req.flush('Bad Request', { status: 400, statusText: 'Bad Request' });
    });

  });
});
