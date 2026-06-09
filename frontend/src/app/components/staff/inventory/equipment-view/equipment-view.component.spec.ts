import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {EquipmentViewComponent} from './equipment-view.component';
import {EquipmentService} from '../../../../services/equipment.service';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {TranslateModule} from '@ngx-translate/core';
import {ActivatedRoute} from '@angular/router';
import {of, throwError} from 'rxjs';
import {registerLocaleData} from '@angular/common';
import localeDe from '@angular/common/locales/de';
import {ReservationService} from '../../../../services/reservation.service';
import {ReservationStatus} from "../../../../dtos/ReservationStatus";

registerLocaleData(localeDe, 'de');

//AI-assisted: Code generated with Google Gemini and adapted
describe('EquipmentViewComponent', () => {
  let component: EquipmentViewComponent;
  let fixture: ComponentFixture<EquipmentViewComponent>;

  let equipmentServiceSpy: jasmine.SpyObj<EquipmentService>;
  let reservationServiceSpy: jasmine.SpyObj<ReservationService>;

  beforeEach(waitForAsync(() => {
    const eqSpy = jasmine.createSpyObj('EquipmentService', ['getById', 'delete']);
    const resSpy = jasmine.createSpyObj('ReservationService', ['search']);
    TestBed.configureTestingModule({
      declarations: [EquipmentViewComponent],
      imports: [
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        {provide: EquipmentService, useValue: eqSpy},
        {provide: ReservationService, useValue: resSpy},
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => '1'
              }
            }
          }
        }
        // ----------------------------------------------------
      ]
    }).compileComponents();

    equipmentServiceSpy = TestBed.inject(EquipmentService) as jasmine.SpyObj<EquipmentService>;
    reservationServiceSpy = TestBed.inject(ReservationService) as jasmine.SpyObj<ReservationService>;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EquipmentViewComponent);
    component = fixture.componentInstance;

    equipmentServiceSpy.getById.and.returnValue(of({
      id: 1,
      barcodeId: '12345',
      price: 25.0,
      model: 'Test Ski',
      status: 'FREE',
      targetSkillLevel: 'BEGINNER',
      equipmentType: 'SKI',
      length: 170
    } as any));

    reservationServiceSpy.search.and.returnValue(of([]));

    fixture.detectChanges();
  });

  it('should create the view component', () => {
    expect(component).toBeTruthy();
  });

  it('should load equipment and immediately call reservation search with equipment id', () => {
    expect(component.equipment).toBeTruthy();
    expect(component.equipment?.id).toBe(1);

    expect(reservationServiceSpy.search).toHaveBeenCalledWith({equipmentIds: [1]});
    expect(component.reservations.length).toBe(0);
    expect(component.reservationsError).toBeFalse();
  });

  it('should successfully store found reservations in the component state', () => {
    reservationServiceSpy.search.and.returnValue(of([
      {
        id: 42,
        customerProfileId: 10,
        accountId: 2,
        customerName: 'Max Mustermann',
        pickUpTime: '09:00',
        startDate: '2026-12-24',
        endDate: '2026-12-26',
        confirmationEmailSent: true,
        items: [],
        reservationStatus: ReservationStatus.CREATED
      }
    ]));

    component['loadReservationsForEquipment'](1);

    expect(component.reservationsLoading).toBeFalse();
    expect(component.reservationsError).toBeFalse();
    expect(component.reservations.length).toBe(1);
    expect(component.reservations[0].customerName).toBe('Max Mustermann');
    expect(component.reservations[0].id).toBe(42);
  });

  it('should set reservationsError to true when reservation search fails', () => {
    reservationServiceSpy.search.and.returnValue(throwError(() => new Error('Backend error')));

    component['loadReservationsForEquipment'](1);

    expect(component.reservationsLoading).toBeFalse();
    expect(component.reservationsError).toBeTrue();
    expect(component.reservations.length).toBe(0);
  });

  it('should call window.print when printBarcode is called', () => {
    spyOn(window, 'print');

    component.printBarcode();

    expect(window.print).toHaveBeenCalled();
  });

  it('should trigger an SVG download when downloadBarcode is called', () => {
    const dummySvg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    spyOn(document, 'getElementById').and.returnValue(dummySvg as any);

    const dummyAnchor = document.createElement('a');
    spyOn(document, 'createElement').and.returnValue(dummyAnchor);
    spyOn(dummyAnchor, 'click');

    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test-url');
    component.equipment = {barcodeId: 'EQUIP-123'} as any;

    component.downloadBarcode();

    expect(dummyAnchor.download).toBe('barcode_EQUIP-123.svg');
    expect(dummyAnchor.href).toBe('blob:test-url');
    expect(dummyAnchor.click).toHaveBeenCalled();
  });

  it('should abort download if barcode svg node is not found', () => {
    spyOn(document, 'getElementById').and.returnValue(null);
    const createObjectURLSpy = spyOn(window.URL, 'createObjectURL');

    component.downloadBarcode();

    expect(createObjectURLSpy).not.toHaveBeenCalled();
  });
});
