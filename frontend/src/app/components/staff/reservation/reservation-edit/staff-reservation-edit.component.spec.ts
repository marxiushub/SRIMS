import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { of, throwError } from 'rxjs';

import { StaffReservationEditComponent } from './staff-reservation-edit.component';
import { ReservationService } from '../../../../services/reservation.service';
import { EquipmentService } from '../../../../services/equipment.service';
import { Equipment } from '../../../../dtos/equipment';
import { EquipmentType } from '../../../../dtos/equipmenttype';
import { SkillLevel } from '../../../../dtos/skilllevel';
import { RentalStatus } from '../../../../dtos/rentalstatus';
import { ReservationStatus } from '../../../../dtos/reservationstatus';
import { ReservationDetail } from '../../../../dtos/reservation-detail';

// AI-assisted: Code generated with Google Gemini and adapted
describe('StaffReservationEditComponent', () => {
  let component: StaffReservationEditComponent;
  let fixture: ComponentFixture<StaffReservationEditComponent>;
  let reservationServiceMock: jasmine.SpyObj<ReservationService>;
  let equipmentServiceMock: jasmine.SpyObj<EquipmentService>;
  let toastrServiceMock: jasmine.SpyObj<ToastrService>;
  let routerMock: jasmine.SpyObj<Router>;

  let activatedRouteMock: {
    snapshot: {
      paramMap: {
        get: jasmine.Spy & ((key: string) => string | null)
      }
    }
  };

  const mockEquipment: Equipment[] = [
    { id: 201, barcodeId: 'BC-001', price: 25.0, model: 'Ski Alpha', status: RentalStatus.FREE, targetSkillLevel: SkillLevel.BEGINNER, equipmentType: EquipmentType.SKI },
    { id: 202, barcodeId: 'BC-002', price: 35.0, model: 'Snowboard Beta', status: RentalStatus.FREE, targetSkillLevel: SkillLevel.ADVANCED, equipmentType: EquipmentType.SNOWBOARD },
    { id: 203, barcodeId: 'BC-003', price: 15.0, model: 'Ski Gamma', status: RentalStatus.FREE, targetSkillLevel: SkillLevel.INTERMEDIATE, equipmentType: EquipmentType.SKI }
  ];

  const mockReservationData: ReservationDetail = {
    id: 42,
    customerProfileId: 101,
    accountId: 10,
    customerName: 'John Doe',
    pickUpTime: '10:00:00',
    startDate: '2026-02-15',
    endDate: '2026-02-20',
    confirmationEmailSent: true,
    totalPrice: 150,
    reservationStatus: ReservationStatus.CREATED,
    items: [mockEquipment[0]]
  };

  beforeEach(async () => {
    reservationServiceMock = jasmine.createSpyObj('ReservationService', ['getById', 'updateForStaff']);
    equipmentServiceMock = jasmine.createSpyObj('EquipmentService', ['search']);
    toastrServiceMock = jasmine.createSpyObj('ToastrService', ['success', 'error', 'warning']);
    routerMock = jasmine.createSpyObj('Router', ['navigate']);

    activatedRouteMock = {
      snapshot: {
        paramMap: jasmine.createSpyObj('paramMap', ['get'])
      }
    };

    reservationServiceMock.getById.and.returnValue(of(mockReservationData));
    equipmentServiceMock.search.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      declarations: [StaffReservationEditComponent],
      imports: [ReactiveFormsModule, TranslateModule.forRoot(), ToastrModule.forRoot()],
      providers: [
        FormBuilder,
        { provide: ReservationService, useValue: reservationServiceMock },
        { provide: EquipmentService, useValue: equipmentServiceMock },
        { provide: ToastrService, useValue: toastrServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StaffReservationEditComponent);
    component = fixture.componentInstance;
  });

  describe('Initialization & Data Loading', () => {
    it('should initialize empty form and watch for route params on init', () => {
      activatedRouteMock.snapshot.paramMap.get.and.returnValue('42');
      fixture.detectChanges();

      expect(component.reservationId).toBe(42);
      expect(reservationServiceMock.getById).toHaveBeenCalledWith(42);
      expect(component.customerName).toBe('John Doe');
      expect(component.customerProfileId).toBe(101);
      expect(component.reservationForm.value).toEqual({
        startDate: '2026-02-15',
        pickUpTime: '10:00',
        endDate: '2026-02-20'
      });
    });

    it('should fallback to 09:00 pickUpTime if none is returned from server', () => {
      activatedRouteMock.snapshot.paramMap.get.and.returnValue('42');
      const missingTimeData = { ...mockReservationData, pickUpTime: '' };
      reservationServiceMock.getById.and.returnValue(of(missingTimeData));

      fixture.detectChanges();
      expect(component.reservationForm.get('pickUpTime')?.value).toBe('09:00');
    });

    it('should set submitError if fetched reservation is null or empty', () => {
      activatedRouteMock.snapshot.paramMap.get.and.returnValue('42');
      reservationServiceMock.getById.and.returnValue(of(null as any));

      fixture.detectChanges();
      expect(component.submitError).toBe('RESERVATION.NOT_FOUND');
    });

    it('should handle load error gracefully via toaster notification', () => {
      spyOn(console, 'error');
      activatedRouteMock.snapshot.paramMap.get.and.returnValue('42');
      reservationServiceMock.getById.and.returnValue(throwError(() => new Error('Server disconnect')));

      fixture.detectChanges();
      expect(toastrServiceMock.error).toHaveBeenCalledWith('Error loading reservation');
      expect(component.submitError).toBe('RESERVATION.LOADING_FAILED');
    });
  });

  describe('Date Range & Equipment Validation', () => {
    beforeEach(() => {
      activatedRouteMock.snapshot.paramMap.get.and.returnValue('42');
      fixture.detectChanges();
      equipmentServiceMock.search.calls.reset();
    });

    it('should calculate date range validity correctly', () => {
      component.reservationForm.patchValue({ startDate: '2026-05-10', endDate: '2026-05-05' });
      expect(component.isDateRangeInvalid).toBeTrue();

      component.reservationForm.patchValue({ startDate: '2026-05-01', endDate: '2026-05-05' });
      expect(component.isDateRangeInvalid).toBeFalse();
    });

    it('should trigger equipment search on form changes only if currentActiveType is set', () => {
      component.currentActiveType = null;
      component.reservationForm.patchValue({ startDate: '2026-03-01' });
      expect(equipmentServiceMock.search).not.toHaveBeenCalled();

      component.currentActiveType = EquipmentType.SKI;
      component.reservationForm.patchValue({ endDate: '2026-03-10' });
      expect(equipmentServiceMock.search).toHaveBeenCalled();
    });

    it('should skip background validation if new date equals loaded original dates', fakeAsync(() => {
      component.selectedEquipment = [mockEquipment[0]];
      component.reservationForm.patchValue({ startDate: '2026-02-15', endDate: '2026-02-20' });
      tick(300);

      expect(equipmentServiceMock.search).not.toHaveBeenCalled();
    }));
  });

  describe('Equipment Search, Filtering and Selection', () => {
    beforeEach(() => {
      activatedRouteMock.snapshot.paramMap.get.and.returnValue('42');
      fixture.detectChanges();
    });

    it('should pass correct filters to equipment service search', () => {
      component.modelFilter = ' Alpha ';
      component.currentActiveType = EquipmentType.SKI;
      component.skillFilter = SkillLevel.BEGINNER;
      equipmentServiceMock.search.and.returnValue(of(mockEquipment));

      component.searchEquipment();

      expect(equipmentServiceMock.search).toHaveBeenCalledWith(jasmine.objectContaining({
        model: 'Alpha',
        type: EquipmentType.SKI,
        targetSkillLevel: SkillLevel.BEGINNER
      }));
    });

    it('should sort equipment items by price in ascending order', () => {
      component.priceSortDirection = 'asc';
      equipmentServiceMock.search.and.returnValue(of(mockEquipment));

      component.searchEquipment();

      expect(component.availableEquipmentList[0].price).toBe(15.0);
      expect(component.availableEquipmentList[2].price).toBe(35.0);
    });

    it('should sort equipment items by price in descending order', () => {
      component.priceSortDirection = 'desc';
      equipmentServiceMock.search.and.returnValue(of(mockEquipment));

      component.searchEquipment();

      expect(component.availableEquipmentList[0].price).toBe(35.0);
      expect(component.availableEquipmentList[2].price).toBe(15.0);
    });

    it('should change price sort direction manually', () => {
      component.availableEquipmentList = [...mockEquipment];
      component.priceSortDirection = 'desc';

      component.onPriceSortChange();
      expect(component.availableEquipmentList[0].price).toBe(35.0);
    });

    it('should clear all active search filters and re-search', () => {
      component.modelFilter = 'Ski';
      component.skillFilter = SkillLevel.INTERMEDIATE;
      component.priceSortDirection = 'desc';
      equipmentServiceMock.search.calls.reset();

      component.clearFilters();

      expect(component.modelFilter).toBe('');
      expect(component.skillFilter).toBeNull();
      expect(component.priceSortDirection).toBe('asc');
      expect(equipmentServiceMock.search).toHaveBeenCalled();
    });

    it('should add item to selected list only if it is not already present', () => {
      component.selectedEquipment = [mockEquipment[0]];

      component.addEquipment(mockEquipment[0]);
      expect(component.selectedEquipment.length).toBe(1);

      component.addEquipment(mockEquipment[1]);
      expect(component.selectedEquipment.length).toBe(2);
    });
  });

  describe('Submit and Cancel Actions', () => {
    beforeEach(() => {
      activatedRouteMock.snapshot.paramMap.get.and.returnValue('42');
      fixture.detectChanges();
    });

    it('should navigate away on cancel()', () => {
      component.cancel();
      expect(routerMock.navigate).toHaveBeenCalledWith(['/staff/reservation']);
    });

    it('should handle confirmation HTMLDialog closure wrapper', () => {
      const dialogElementSpy = jasmine.createSpyObj<HTMLDialogElement>('HTMLDialogElement', ['close']);
      spyOn(component, 'submitReservation');

      component.confirmAndSubmit(dialogElementSpy);

      expect(dialogElementSpy.close).toHaveBeenCalled();
      expect(component.submitReservation).toHaveBeenCalled();
    });

    it('should abort submission if the reactive form is invalid', () => {
      component.reservationForm.patchValue({ startDate: null });
      component.submitReservation();
      expect(reservationServiceMock.updateForStaff).not.toHaveBeenCalled();
    });

    it('should stop submission and issue warning if no items are selected', () => {
      component.selectedEquipment = [];
      component.submitReservation();
      expect(toastrServiceMock.warning).toHaveBeenCalledWith('Please choose at least 1 piece of equipment');
      expect(reservationServiceMock.updateForStaff).not.toHaveBeenCalled();
    });

    it('should stop submission and show error toast if date bounds are mismatched', () => {
      component.selectedEquipment = [mockEquipment[0]];
      component.reservationForm.patchValue({ startDate: '2026-05-20', endDate: '2026-05-10' });

      component.submitReservation();

      expect(toastrServiceMock.error).toHaveBeenCalledWith('The return date cannot be before the pick-up date');
      expect(reservationServiceMock.updateForStaff).not.toHaveBeenCalled();
    });

    it('should send well-formatted payload on valid submission and navigate to list', () => {
      component.selectedEquipment = [mockEquipment[0], mockEquipment[1]];
      component.reservationForm.patchValue({
        startDate: '2026-04-01',
        endDate: '2026-04-05',
        pickUpTime: '08:30'
      });
      reservationServiceMock.updateForStaff.and.returnValue(of(mockReservationData));

      const translateService = TestBed.inject(TranslateService);
      spyOn(translateService, 'instant').and.returnValue('Successfully updated');

      component.submitReservation();

      expect(reservationServiceMock.updateForStaff).toHaveBeenCalledWith(42, {
        id: 42,
        customerProfileId: 101,
        equipmentIds: [201, 202],
        pickUpTime: '08:30:00',
        startDate: '2026-04-01',
        endDate: '2026-04-05'
      });
      expect(routerMock.navigate).toHaveBeenCalledWith(['/staff/reservation']);
      expect(toastrServiceMock.success).toHaveBeenCalledWith('Successfully updated');
    });

    it('should map exception message payload into state error property on submission failure', () => {
      spyOn(console, 'error');
      component.selectedEquipment = [mockEquipment[0]];
      reservationServiceMock.updateForStaff.and.returnValue(throwError(() => ({ error: { message: 'Overlapping reservation constraint' } })));

      component.submitReservation();

      expect(component.submitError).toBe('Overlapping reservation constraint');
      expect(component.submitLoading).toBeFalse();
    });

    it('should fall back to standard error message text if exception body contains no descriptive text', () => {
      spyOn(console, 'error');
      component.selectedEquipment = [mockEquipment[0]];
      reservationServiceMock.updateForStaff.and.returnValue(throwError(() => ({ error: {} })));

      component.submitReservation();

      expect(component.submitError).toBe('An error occurred while updating the reservation.');
      expect(component.submitLoading).toBeFalse();
    });
  });

  describe('Live Price Calculation', () => {
    beforeEach(() => {
      activatedRouteMock.snapshot.paramMap.get.and.returnValue('42');
      fixture.detectChanges();
    });

    it('should return 0 if no equipment is selected', () => {
      component.selectedEquipment = [];
      component.reservationForm.patchValue({
        startDate: '2026-02-15',
        endDate: '2026-02-17'
      });

      expect(component.currentTotalPrice).toBe(0);
    });

    it('should return 0 if startDate or endDate is missing', () => {
      component.selectedEquipment = [mockEquipment[0]]; // 25.0 €

      component.reservationForm.patchValue({startDate: null, endDate: '2026-02-17'});
      expect(component.currentTotalPrice).toBe(0);

      component.reservationForm.patchValue({startDate: '2026-02-15', endDate: null});
      expect(component.currentTotalPrice).toBe(0);
    });

    it('should return 0 if the date range is invalid (end before start)', () => {
      component.selectedEquipment = [mockEquipment[0]];
      component.reservationForm.patchValue({
        startDate: '2026-02-15',
        endDate: '2026-02-14'
      });

      expect(component.currentTotalPrice).toBe(0);
    });

    it('should calculate the correct price for a single day rental (same start and end date)', () => {
      component.selectedEquipment = [mockEquipment[0]];
      component.reservationForm.patchValue({
        startDate: '2026-02-15',
        endDate: '2026-02-15'
      });

      expect(component.currentTotalPrice).toBe(25.0);
    });

    it('should calculate the correct cumulative price for multiple items over multiple days', () => {
      component.selectedEquipment = [mockEquipment[0], mockEquipment[1]];

      component.reservationForm.patchValue({
        startDate: '2026-02-15',
        endDate: '2026-02-17'
      });

      expect(component.currentTotalPrice).toBe(180.0);
    });
  });
});
