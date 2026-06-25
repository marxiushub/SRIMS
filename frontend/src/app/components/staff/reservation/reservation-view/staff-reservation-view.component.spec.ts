import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {ToastrModule, ToastrService} from 'ngx-toastr';
import {of, throwError} from 'rxjs';

import {StaffReservationViewComponent} from './staff-reservation-view.component';
import {ReservationService} from '../../../../services/reservation.service';
import {ReservationStatus} from '../../../../dtos/reservationstatus';
import {ReservationDetail} from '../../../../dtos/reservation-detail';

// AI-assisted: Code generated with Google Gemini and adapted
describe('StaffReservationViewComponent', () => {
  let component: StaffReservationViewComponent;
  let fixture: ComponentFixture<StaffReservationViewComponent>;
  let reservationServiceMock: jasmine.SpyObj<ReservationService>;
  let toastrServiceMock: jasmine.SpyObj<ToastrService>;
  let routerMock: jasmine.SpyObj<Router>;

  // Custom mock for ActivatedRoute to simulate path params
  let activatedRouteMock: {
    snapshot: {
      paramMap: {
        get: jasmine.Spy & ((key: string) => string | null)
      }
    }
  };

  const mockReservation: ReservationDetail = {
    id: 42,
    accountId: 10,
    customerProfileId: 101,
    customerName: 'John Doe',
    pickUpTime: '10:00:00',
    startDate: '2026-02-15',
    endDate: '2026-02-20',
    confirmationEmailSent: true,
    totalPrice: 0,
    items: [],
    reservationStatus: ReservationStatus.CREATED
  };

  beforeEach(async () => {
    reservationServiceMock = jasmine.createSpyObj('ReservationService', ['getById', 'deleteForStaff']);
    toastrServiceMock = jasmine.createSpyObj('ToastrService', ['success', 'error']);
    routerMock = jasmine.createSpyObj('Router', ['navigate']);

    // Set up paramMap spy for snapshot path lookups
    activatedRouteMock = {
      snapshot: {
        paramMap: jasmine.createSpyObj('paramMap', ['get'])
      }
    };

    await TestBed.configureTestingModule({
      declarations: [StaffReservationViewComponent],
      imports: [TranslateModule.forRoot(), ToastrModule.forRoot()],
      providers: [
        {provide: ReservationService, useValue: reservationServiceMock},
        {provide: ToastrService, useValue: toastrServiceMock},
        {provide: Router, useValue: routerMock},
        {provide: ActivatedRoute, useValue: activatedRouteMock}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StaffReservationViewComponent);
    component = fixture.componentInstance;
  });

  describe('Initialization (ngOnInit)', () => {
    it('should load reservation details if a valid ID is present in the route params', () => {
      activatedRouteMock.snapshot.paramMap.get.and.returnValue('42');
      reservationServiceMock.getById.and.returnValue(of(mockReservation));

      fixture.detectChanges(); // Triggers ngOnInit()

      expect(component.reservationId).toBe(42);
      expect(reservationServiceMock.getById).toHaveBeenCalledWith(42);
      expect(component.reservation).toEqual(mockReservation);
      expect(component.loading).toBeFalse();
      expect(component.error).toBeUndefined();
    });

    it('should set error state and show toast notification if ID is missing in route params', () => {
      activatedRouteMock.snapshot.paramMap.get.and.returnValue(null);

      fixture.detectChanges(); // Triggers ngOnInit()

      expect(component.error).toBe('RESERVATION.NOT_FOUND');
      expect(toastrServiceMock.error).toHaveBeenCalledWith('No valid reservation ID provided.');
      expect(reservationServiceMock.getById).not.toHaveBeenCalled();
    });

    it('should set loading failed error state if backend call fails', () => {
      spyOn(console, 'error');
      activatedRouteMock.snapshot.paramMap.get.and.returnValue('42');
      reservationServiceMock.getById.and.returnValue(throwError(() => new Error('Not Found')));

      fixture.detectChanges(); // Triggers ngOnInit()

      expect(component.loading).toBeFalse();
      expect(component.error).toBe('RESERVATION.LOADING_FAILED');
      expect(component.reservation).toBeUndefined();
    });
  });

  describe('Navigation & Detail Paths', () => {
    beforeEach(() => {
      component.reservationId = 42;
    });

    it('should navigate back to the reservation list on backToList()', () => {
      component.backToList();
      expect(routerMock.navigate).toHaveBeenCalledWith(['/staff/reservation']);
    });

    it('should navigate to the correct edit page on openEditPage()', () => {
      component.openEditPage();
      expect(routerMock.navigate).toHaveBeenCalledWith(['/staff/reservation/edit', 42]);
    });
  });

  describe('Delete Operations', () => {
    beforeEach(() => {
      component.reservationId = 42;
      component.reservation = mockReservation;
    });

    it('should open delete modal dialog and clear previous errors', () => {
      component.deleteError = 'Previous Error';
      component.openDeleteDialog();

      expect(component.showDeleteModal).toBeTrue();
      expect(component.deleteError).toBeUndefined();
    });

    it('should cancel delete modal dialog and reset states', () => {
      component.showDeleteModal = true;
      component.deleteError = 'Some Error';
      component.deleteLoading = true;

      component.cancelDelete();

      expect(component.showDeleteModal).toBeFalse();
      expect(component.deleteError).toBeUndefined();
      expect(component.deleteLoading).toBeFalse();
    });

    it('should do nothing on confirmDelete if reservation state is not loaded', () => {
      component.reservation = undefined;
      component.confirmDelete();

      expect(reservationServiceMock.deleteForStaff).not.toHaveBeenCalled();
    });

    it('should successfully delete reservation, close modal, show success toast, and navigate back to list', () => {
      spyOn(console, 'error');
      reservationServiceMock.deleteForStaff.and.returnValue(of(void 0));
      spyOn(component, 'backToList');

      const translateService = TestBed.inject(TranslateService);
      spyOn(translateService, 'instant').and.returnValue('Successfully deleted');

      component.confirmDelete();

      expect(reservationServiceMock.deleteForStaff).toHaveBeenCalledWith(42);
      expect(component.showDeleteModal).toBeFalse();
      expect(component.deleteLoading).toBeFalse();
      expect(toastrServiceMock.success).toHaveBeenCalledWith('Successfully deleted');
      expect(component.backToList).toHaveBeenCalled();
    });

    it('should handle backend error on delete and set deleteError message', () => {
      spyOn(console, 'error');
      component.showDeleteModal = true;
      reservationServiceMock.deleteForStaff.and.returnValue(throwError(() => ({error: {message: 'Cannot delete'}})));

      component.confirmDelete();

      expect(component.deleteError).toBe('Cannot delete');
      expect(component.deleteLoading).toBeFalse();
      expect(component.showDeleteModal).toBeTrue(); // Modal remains open
    });

    it('should fallback to default error message if error payload contains no message text', () => {
      spyOn(console, 'error');
      component.showDeleteModal = true;
      reservationServiceMock.deleteForStaff.and.returnValue(throwError(() => ({error: {}})));

      component.confirmDelete();

      expect(component.deleteError).toBe('Reservation could not be deleted.');
      expect(component.deleteLoading).toBeFalse();
      expect(component.showDeleteModal).toBeTrue();
    });
  });
});
