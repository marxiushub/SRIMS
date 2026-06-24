import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';

import { StatisticsComponent } from './statistics.component';
import { StatisticsService } from '../../../services/statistics.service';
import { StatisticsResponseDto } from '../../../dtos/statistics-response';
import { EquipmentType } from '../../../dtos/equipmenttype';

describe('StatisticsComponent', () => {
  let component: StatisticsComponent;
  let fixture: ComponentFixture<StatisticsComponent>;
  let statisticsServiceMock: jasmine.SpyObj<StatisticsService>;

  const mockModelResponse: StatisticsResponseDto = {
    detailDegree: false,
    modelCounts: {
      'Ski Super-G': 25,
      'Helmet Safety First': 5,
      'Snowboard Freeride': 12
    },
    itemCounts: undefined
  };

  const mockItemResponse: StatisticsResponseDto = {
    detailDegree: true,
    modelCounts: undefined,
    itemCounts: {
      '101': 3,
      '102': 7,
      '103': 1
    }
  };

  beforeEach(async () => {
    statisticsServiceMock = jasmine.createSpyObj('StatisticsService', ['getEquipmentStatistics']);
    statisticsServiceMock.getEquipmentStatistics.and.returnValue(of({
      detailDegree: false,
      modelCounts: {},
      itemCounts: undefined
    }));

    await TestBed.configureTestingModule({
      declarations: [StatisticsComponent],
      imports: [
        ReactiveFormsModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: StatisticsService, useValue: statisticsServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StatisticsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Initialization', () => {
    it('should initialize the filter form with default values (30 days range)', () => {
      const form = component.filterForm;
      expect(form).toBeTruthy();
      expect(form.get('type')?.value).toBe('');
      expect(form.get('detailDegree')?.value).toBeFalse();

      const start = new Date(form.get('searchStart')?.value);
      const end = new Date(form.get('searchEnd')?.value);

      // Differenz in Tagen berechnen (ca. 30 Tage)
      const diffTime = Math.abs(end.getTime() - start.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      expect(diffDays).toBe(30);
    });
  });

  describe('onSubmit - Form Handling & API Requests', () => {
    it('should not call the service if the form is invalid', () => {
      component.filterForm.get('searchStart')?.setValue(''); // Formular invalid machen
      component.onSubmit();
      expect(statisticsServiceMock.getEquipmentStatistics).not.toHaveBeenCalled();
    });

    it('should map empty type string to null before sending the request', () => {
      component.filterForm.patchValue({
        searchStart: '2026-01-01',
        searchEnd: '2026-01-31',
        type: ''
      });

      component.onSubmit();

      expect(statisticsServiceMock.getEquipmentStatistics).toHaveBeenCalledWith({
        searchStart: '2026-01-01',
        searchEnd: '2026-01-31',
        type: null,
        detailDegree: false
      });
    });

    it('should map modelCounts correctly and sort descending when detailDegree is false', () => {
      statisticsServiceMock.getEquipmentStatistics.and.returnValue(of(mockModelResponse));

      component.filterForm.patchValue({ detailDegree: false });
      component.onSubmit();

      expect(component.isLoading).toBeFalse();
      expect(component.tableRows.length).toBe(3);
      //25 -> 12 -> 5
      expect(component.tableRows[0]).toEqual({ label: 'Ski Super-G', daysRented: 25 });
      expect(component.tableRows[1]).toEqual({ label: 'Snowboard Freeride', daysRented: 12 });
      expect(component.tableRows[2]).toEqual({ label: 'Helmet Safety First', daysRented: 5 });
    });

    it('should map itemCounts correctly with prefixed hashtag when detailDegree is true', () => {
      statisticsServiceMock.getEquipmentStatistics.and.returnValue(of(mockItemResponse));

      component.filterForm.patchValue({ detailDegree: true });
      component.onSubmit();

      expect(component.tableRows.length).toBe(3);

      expect(component.tableRows[0]).toEqual({ label: '#102', daysRented: 7, id: '102' });
      expect(component.tableRows[1]).toEqual({ label: '#101', daysRented: 3, id: '101' });
      expect(component.tableRows[2]).toEqual({ label: '#103', daysRented: 1, id: '103' });
    });

    it('should handle API errors gracefully and set errorMessageKey', () => {
      spyOn(console, 'error');
      statisticsServiceMock.getEquipmentStatistics.and.returnValue(throwError(() => new Error('API Error')));

      component.onSubmit();

      expect(component.isLoading).toBeFalse();
      expect(component.errorMessageKey).toBe('STAFF.STATISTICS.MESSAGES.ERROR');
      expect(component.tableRows.length).toBe(0);
    });
  });

  describe('Sorting Logic', () => {
    beforeEach(() => {
      component.tableRows = [
        { label: 'Model A', daysRented: 10 },
        { label: 'Model B', daysRented: 40 },
        { label: 'Model C', daysRented: 25 }
      ];
    });

    it('should toggle sort direction and sort rows correctly', () => {
      expect(component.sortDescending).toBeTrue();


      component.toggleSort();
      expect(component.sortDescending).toBeFalse();
      expect(component.tableRows[0].daysRented).toBe(10);
      expect(component.tableRows[2].daysRented).toBe(40);


      component.toggleSort();
      expect(component.sortDescending).toBeTrue();
      expect(component.tableRows[0].daysRented).toBe(40);
      expect(component.tableRows[2].daysRented).toBe(10);
    });
  });

  describe('Y-Axis Scale Calculation', () => {
    it('should generate linear 1-step ticks if max value is 10 or less', () => {
      component.tableRows = [
        { label: 'Low Rent Item', daysRented: 6 }
      ];


      component['calculateYAxis']();

      expect(component.maxDaysRented).toBe(6);
      expect(component.yAxisTicks).toEqual([0, 1, 2, 3, 4, 5, 6]);
    });

    it('should generate rounded, beautiful step ticks if max value is greater than 10', () => {
      component.tableRows = [
        { label: 'High Rent Item', daysRented: 42 }
      ];

      component['calculateYAxis']();


      expect(component.maxDaysRented).toBe(45);
      expect(component.yAxisTicks).toEqual([0, 9, 18, 27, 36, 45]);
    });

    it('should default maxDaysRented to 1 and ticks to [0, 1] if no data is available', () => {
      component.tableRows = [];

      component['calculateYAxis']();

      expect(component.maxDaysRented).toBe(1);
      expect(component.yAxisTicks).toEqual([0, 1]);
    });
  });
});
