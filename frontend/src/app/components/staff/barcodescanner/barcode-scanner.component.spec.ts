import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule, FormsModule, FormBuilder } from '@angular/forms';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { ZXingScannerModule } from '@zxing/ngx-scanner';

import { BarcodeScannerComponent } from './barcode-scanner.component';
import { EquipmentService } from '../../../services/equipment.service';
import { ReservationService } from '../../../services/reservation.service';
import { BarcodeScannerService } from '../../../services/barcode-scanner.service';
import { CustomerProfileService } from '../../../services/customer-profile.service';
import { StaffService } from '../../../services/staff.service';
import { ReservationStatus } from '../../../dtos/reservationstatus';
import { Equipment } from '../../../dtos/equipment';
import { ReservationDetail } from '../../../dtos/reservation-detail';

// AI-assisted: Code generated with Google Gemini and adapted
describe('BarcodeScannerComponent', () => {
  let component: BarcodeScannerComponent;
  let fixture: ComponentFixture<BarcodeScannerComponent>;

  let equipmentServiceMock: jasmine.SpyObj<EquipmentService>;
  let reservationServiceMock: jasmine.SpyObj<ReservationService>;
  let barcodeScannerServiceMock: jasmine.SpyObj<BarcodeScannerService>;
  let customerProfileServiceMock: jasmine.SpyObj<CustomerProfileService>;
  let staffServiceMock: jasmine.SpyObj<StaffService>;
  let toastrServiceMock: jasmine.SpyObj<ToastrService>;

  const mockEquipment1: Equipment = {
    id: 42,
    model: 'Ski Alpine Alpha',
    barcodeId: '12345678',
    equipmentType: 'SKI' as any,
    targetSkillLevel: 'ADVANCED' as any,
    price: 20.0,
    status: 'FREE' as any
  };

  const mockEquipment2: Equipment = {
    id: 43,
    model: 'Snowboard Beta',
    barcodeId: '87654321',
    equipmentType: 'SNOWBOARD' as any,
    targetSkillLevel: 'BEGINNER' as any,
    price: 15.0,
    status: 'RESERVED' as any
  };

  const mockReservation: ReservationDetail = {
    id: 101,
    accountId: 1,
    customerProfileId: 1,
    customerName: 'Max Mustermann',
    startDate: '2026-06-25',
    pickUpTime: "09:00",
    endDate: '2026-06-27',
    confirmationEmailSent: false,
    totalPrice: 20,
    reservationStatus: ReservationStatus.CREATED,
    items: [mockEquipment1]
  };

  beforeEach(async () => {
    equipmentServiceMock = jasmine.createSpyObj('EquipmentService', ['getByBarcodeId']);
    reservationServiceMock = jasmine.createSpyObj('ReservationService', ['search', 'addEquipmentToReservation', 'removeEquipmentFromReservation']);
    barcodeScannerServiceMock = jasmine.createSpyObj('BarcodeScannerService', ['checkOutOrInScanWithExistingReservation', 'checkOutScanWithoutExistingReservation']);
    customerProfileServiceMock = jasmine.createSpyObj('CustomerProfileService', ['getCustomerProfilesByCustomerId']);
    staffServiceMock = jasmine.createSpyObj('StaffService', ['searchCustomers']);
    toastrServiceMock = jasmine.createSpyObj('ToastrService', ['success', 'error', 'warning', 'info']);

    staffServiceMock.searchCustomers.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      declarations: [BarcodeScannerComponent],
      imports: [
        RouterModule.forRoot([]),
        TranslateModule.forRoot(),
        ToastrModule.forRoot(),
        ReactiveFormsModule,
        FormsModule,
        ZXingScannerModule
      ],
      providers: [
        FormBuilder,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: EquipmentService, useValue: equipmentServiceMock },
        { provide: ReservationService, useValue: reservationServiceMock },
        { provide: BarcodeScannerService, useValue: barcodeScannerServiceMock },
        { provide: CustomerProfileService, useValue: customerProfileServiceMock },
        { provide: StaffService, useValue: staffServiceMock },
        { provide: ToastrService, useValue: toastrServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BarcodeScannerComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create the component and initialize the walk-in form', () => {
    expect(component).toBeTruthy();
    expect(component.walkInForm).toBeDefined();
  });

  // --- Camera Stream Handlers ---
  describe('Camera Handlers', () => {
    it('should open and close camera stream visibility flags', () => {
      component.openCamera();
      expect(component.isCameraOpen).toBeTrue();
      component.closeCamera();
      expect(component.isCameraOpen).toBeFalse();
    });

    it('should handle onCodeResult and prevent immediate multi-scans via timeout', fakeAsync(() => {
      spyOn(component, 'searchEquipment');
      component.isScanningPaused = false;

      component.onCodeResult('987654321');

      expect(component.inputBarcodeId).toBe('987654321');
      expect(component.isScanningPaused).toBeTrue();
      expect(component.searchEquipment).toHaveBeenCalled();

      tick(1000);
      expect(component.isScanningPaused).toBeFalse();
    }));
  });

  // --- Scan Scenarios & Business Logic ---
  describe('searchEquipment & Scenario Updates', () => {
    it('should add scanned equipment and match existing reservation if found', fakeAsync(() => {
      component.inputBarcodeId = '12345678';
      equipmentServiceMock.getByBarcodeId.and.returnValue(of(mockEquipment1));
      reservationServiceMock.search.and.returnValue(of([mockReservation]));

      component.searchEquipment();
      tick();

      expect(component.scannedEquipments).toContain(mockEquipment1);
      expect(component.scannedEquipmentIds).toContain(mockEquipment1.id);
      expect(component.matchedReservations).toContain(mockReservation);
      expect(component.scanScenario).toBe('SINGLE_RESERVATION');
      expect(component.equipmentScenario).toBe('ALL_RESERVED_EQUIPMENT_SCANNED');
    }));

    it('should alert error if equipment is already scanned', () => {
      component.scannedEquipmentIds = [mockEquipment1.id];
      component.inputBarcodeId = '12345678';
      equipmentServiceMock.getByBarcodeId.and.returnValue(of(mockEquipment1));

      component.searchEquipment();

      expect(toastrServiceMock.error).toHaveBeenCalled();
    });

    it('should switch to NO_RESERVATION scenario when no matching reservation is found today', fakeAsync(() => {
      component.inputBarcodeId = '12345678';
      equipmentServiceMock.getByBarcodeId.and.returnValue(of(mockEquipment1));
      reservationServiceMock.search.and.returnValue(of([]));

      component.searchEquipment();
      tick();

      expect(component.scanScenario).toBe('NO_RESERVATION');
    }));
  });

  // --- Session Modification & Reservation Sync ---
  describe('Equipment Modification in Current Session', () => {
    it('should clear equipment from active scan session list locally upon removeEquipmentFromList', () => {
      component.scannedEquipments = [mockEquipment1];
      component.scannedEquipmentIds = [mockEquipment1.id];
      component.matchedReservations = [mockReservation];

      component.removeEquipmentFromList(mockEquipment1);

      expect(component.scannedEquipments.length).toBe(0);
      expect(component.scannedEquipmentIds.length).toBe(0);
    });

    it('should invoke reservationService.removeEquipmentFromReservation when removing item directly from backend reservation', () => {
      component.matchedReservations = [mockReservation];
      component.scanScenario = 'SINGLE_RESERVATION';
      reservationServiceMock.removeEquipmentFromReservation.and.returnValue(of({ ...mockReservation, items: [] }));

      component.removeEquipmentFromExistingReservation(mockEquipment1.id);

      expect(reservationServiceMock.removeEquipmentFromReservation).toHaveBeenCalledWith({
        id: mockReservation.id,
        equipmentIds: [mockEquipment1.id]
      });
    });

    it('should invoke reservationService.addEquipmentToReservation when appending unreserved scanned equipment', () => {
      component.matchedReservations = [mockReservation];
      reservationServiceMock.addEquipmentToReservation.and.returnValue(of(mockReservation));

      component.addEquipmentToExistingReservation(mockEquipment1.id);

      expect(reservationServiceMock.addEquipmentToReservation).toHaveBeenCalledWith({
        id: mockReservation.id,
        equipmentIds: [mockEquipment1.id]
      });
    });
  });

  // --- Dynamic Pricing & Visual Helpers ---
  describe('scannedItemsTotalPrice Calculation', () => {
    it('should return 0 if no equipment is scanned', () => {
      component.scannedEquipments = [];
      expect(component.scannedItemsTotalPrice).toBe(0);
    });

    it('should calculate price based on reservation runtime if matchedReservations has exactly 1 entry', () => {
      component.scanScenario = 'SINGLE_RESERVATION';
      component.matchedReservations = [mockReservation];
      component.scannedEquipments = [mockEquipment1];

      expect(component.scannedItemsTotalPrice).toBe(60);
    });

    it('should fallback to 1 day runtime if scanScenario is single/conflict but criteria list bounds are unmatched', () => {
      component.scanScenario = 'CONFLICT_RESERVATION';
      component.matchedReservations = [];
      component.scannedEquipments = [mockEquipment1];

      expect(component.scannedItemsTotalPrice).toBe(20);
    });
  });

  describe('getStatusClass Style Mapper', () => {
    it('should map FREE to bg-success', () => {
      expect(component.getStatusClass('FREE')).toBe('bg-success');
    });

    it('should map RESERVED to bg-warning text-dark', () => {
      expect(component.getStatusClass('RESERVED')).toBe('bg-warning text-dark');
    });

    it('should map RENTED to bg-danger', () => {
      expect(component.getStatusClass('RENTED')).toBe('bg-danger');
    });

    it('should map MAINTENANCE to bg-secondary', () => {
      expect(component.getStatusClass('MAINTENANCE')).toBe('bg-secondary');
    });

    it('should fallback to bg-light text-dark on unknown status values', () => {
      expect(component.getStatusClass('UNKNOWN_STATUS')).toBe('bg-light text-dark');
    });
  });

  // --- Submission Orchestration ---
  describe('Reservation Submissions', () => {
    it('should submit existing reservation updates when scenario criteria are satisfied', () => {
      component.matchedReservations = [{ ...mockReservation, reservationStatus: ReservationStatus.CREATED }];
      component.scanScenario = 'SINGLE_RESERVATION';
      component.equipmentScenario = 'ALL_RESERVED_EQUIPMENT_SCANNED';
      component.scannedEquipmentIds = [mockEquipment1.id];

      barcodeScannerServiceMock.checkOutOrInScanWithExistingReservation.and.returnValue(of({} as any));

      component.submitExistingReservation();

      expect(barcodeScannerServiceMock.checkOutOrInScanWithExistingReservation).toHaveBeenCalledWith({
        id: mockReservation.id,
        reservationStatus: ReservationStatus.PICKED_UP,
        equipmentIds: [mockEquipment1.id]
      });
    });

    it('should block walk-in form submission if end-date range check fails', () => {
      component.walkInForm.patchValue({ endDate: '2020-01-01' });
      spyOn(component, 'submitWalkInCheckout').and.callThrough();

      expect(component.isWalkInDateRangeInvalid).toBeTrue();
    });
  });
});
