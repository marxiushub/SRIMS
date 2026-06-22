import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { of, throwError } from 'rxjs';

import { StaffReservationComponent } from './staff-reservation.component';
import { ReservationService } from '../../../services/reservation.service';
import { StaffService } from '../../../services/staff.service';
import { ReservationStatus } from '../../../dtos/reservationstatus';
import { ReservationDetail } from '../../../dtos/reservation-detail';
import { CustomerSearchResponse } from '../../../dtos/customer-search-response';
import { UserType } from '../../../dtos/usertype';

// AI-assisted: Code generated with Google Gemini and adapted
describe('StaffReservationComponent', () => {
  let component: StaffReservationComponent;
  let fixture: ComponentFixture<StaffReservationComponent>;
  let reservationServiceMock: jasmine.SpyObj<ReservationService>;
  let staffServiceMock: jasmine.SpyObj<StaffService>;
  let toastrServiceMock: jasmine.SpyObj<ToastrService>;
  let routerMock: jasmine.SpyObj<Router>;

  const mockReservations: ReservationDetail[] = [
    { id: 1, accountId: 10, customerProfileId: 101, customerName: 'John Doe', pickUpTime: '10:00:00', startDate: '2026-02-15', endDate: '2026-02-20', confirmationEmailSent: true, items: [], reservationStatus: ReservationStatus.CREATED },
    { id: 2, accountId: 11, customerProfileId: 102, customerName: 'Jane Doe', pickUpTime: '11:00:00', startDate: '2026-02-16', endDate: '2026-02-21', confirmationEmailSent: true, items: [], reservationStatus: ReservationStatus.PICKED_UP },
    { id: 3, accountId: 12, customerProfileId: 103, customerName: 'Bob Smith', pickUpTime: '12:00:00', startDate: '2026-02-17', endDate: '2026-02-22', confirmationEmailSent: true, items: [], reservationStatus: ReservationStatus.RETURNED }
  ];

  const mockCustomerResponse: CustomerSearchResponse = {
    id: 99,
    userName: 'unique1',
    email: 'u@test.com',
    userType: UserType.CUSTOMER as any, // Cast to any to safely match whatever your app's UserType is
    firstName: 'Unique',
    lastName: 'User'
  };

  beforeEach(async () => {
    reservationServiceMock = jasmine.createSpyObj('ReservationService', ['search', 'delete']);
    staffServiceMock = jasmine.createSpyObj('StaffService', ['searchCustomers']);
    toastrServiceMock = jasmine.createSpyObj('ToastrService', ['success', 'error']);
    routerMock = jasmine.createSpyObj('Router', ['navigate']);

    reservationServiceMock.search.and.returnValue(of([]));
    staffServiceMock.searchCustomers.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      declarations: [StaffReservationComponent],
      imports: [FormsModule, TranslateModule.forRoot(), ToastrModule.forRoot()],
      providers: [
        { provide: ReservationService, useValue: reservationServiceMock },
        { provide: StaffService, useValue: staffServiceMock },
        { provide: ToastrService, useValue: toastrServiceMock },
        { provide: Router, useValue: routerMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StaffReservationComponent);
    component = fixture.componentInstance;
  });

  it('should create and load initial reservations on init', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
    expect(reservationServiceMock.search).toHaveBeenCalled();
  });

  describe('loadReservations() Branch-Testing', () => {
    beforeEach(() => {
      fixture.detectChanges();
      reservationServiceMock.search.calls.reset();
    });

    it('Case 1: should filter only by dropdown status and ignore slider state', () => {
      component.statusFilter = ReservationStatus.RETURNED;
      component.showPastReservations = false;

      reservationServiceMock.search.and.returnValue(of([mockReservations[2]]));

      component.loadReservations();

      expect(reservationServiceMock.search).toHaveBeenCalledOnceWith(jasmine.objectContaining({
        reservationStatus: ReservationStatus.RETURNED
      }));
      expect(component.reservations).toEqual([mockReservations[2]]);
    });

    it('Case 2: should use forkJoin for CREATED and PICKED_UP when no status is selected and slider is false', () => {
      component.statusFilter = null;
      component.showPastReservations = false;

      reservationServiceMock.search.and.returnValues(
        of([mockReservations[0]]),
        of([mockReservations[1]])
      );

      component.loadReservations();

      expect(reservationServiceMock.search).toHaveBeenCalledTimes(2);
      expect(reservationServiceMock.search).toHaveBeenCalledWith(jasmine.objectContaining({ reservationStatus: ReservationStatus.CREATED }));
      expect(reservationServiceMock.search).toHaveBeenCalledWith(jasmine.objectContaining({ reservationStatus: ReservationStatus.PICKED_UP }));
      expect(component.reservations).toEqual([mockReservations[0], mockReservations[1]]);
    });

    it('Case 3: should send a single request without status filter when no status is selected and slider is true', () => {
      component.statusFilter = null;
      component.showPastReservations = true;

      reservationServiceMock.search.and.returnValue(of(mockReservations));

      component.loadReservations();

      expect(reservationServiceMock.search).toHaveBeenCalledOnceWith({
        startDate: undefined,
        pickUpTime: undefined,
        reservationStatus: undefined,
        accountId: undefined
      });
      expect(component.reservations).toEqual(mockReservations);
    });

    it('should handle errors gracefully and show a toaster notification on failure', () => {
      spyOn(console, 'error');
      component.statusFilter = ReservationStatus.CREATED;
      reservationServiceMock.search.and.returnValue(throwError(() => new Error('Server Offline')));

      component.loadReservations();

      expect(component.loading).toBeFalse();
      expect(toastrServiceMock.error).toHaveBeenCalledWith('Failed to load reservations from server.');
    });
  });

  describe('Customer Filters & Background Lookups', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should debounce background customer search when criteria changes', fakeAsync(() => {
      component.customerSearchCriteria.lastName = 'Müller';
      staffServiceMock.searchCustomers.and.returnValue(of([]));

      component.onCustomerFilterChange();
      expect(staffServiceMock.searchCustomers).not.toHaveBeenCalled();

      tick(400);
      expect(staffServiceMock.searchCustomers).toHaveBeenCalled();
    }));

    it('should clear selection and reload if all customer search inputs are empty', () => {
      component.selectedCustomerId = 123;
      component.customerSearchCriteria = { firstName: '', lastName: '', email: '', userName: '' };
      reservationServiceMock.search.calls.reset();

      component.onCustomerFilterChange();

      expect(component.selectedCustomerId).toBeNull();
      expect(component.selectedCustomerAccount).toBeNull();
      expect(component.foundCustomers).toEqual([]);
      expect(reservationServiceMock.search).toHaveBeenCalled();
    });

    it('should auto-select customer and apply accountId filter if search returns exactly one result', fakeAsync(() => {
      component.customerSearchCriteria.lastName = 'Unique';
      staffServiceMock.searchCustomers.and.returnValue(of([mockCustomerResponse]));
      reservationServiceMock.search.calls.reset();

      component.onCustomerFilterChange();
      tick(400); // Debounce abwarten

      expect(component.selectedCustomerId).toBe(99);
      expect(reservationServiceMock.search).toHaveBeenCalledWith(jasmine.objectContaining({
        accountId: 99
      }));
    }));

    it('should handle error when customer search fails', fakeAsync(() => {
      spyOn(console, 'error');
      component.customerSearchCriteria.lastName = 'Error';
      staffServiceMock.searchCustomers.and.returnValue(throwError(() => new Error('DB Error')));

      component.onCustomerFilterChange();
      tick(400);

      expect(component.searchingCustomers).toBeFalse();
    }));

    it('should update state and reload when a customer is manually selected by ID', () => {
      component.foundCustomers = [mockCustomerResponse];
      reservationServiceMock.search.calls.reset();

      component.onCustomerSelectById(99);

      expect(component.selectedCustomerId).toBe(99);
      expect(component.selectedCustomerAccount).toEqual(mockCustomerResponse);
      expect(reservationServiceMock.search).toHaveBeenCalled();
    });

    it('should pass the selected customer accountId to reservationService when manually selected', () => {
      component.foundCustomers = [mockCustomerResponse];
      reservationServiceMock.search.calls.reset();

      component.onCustomerSelectById(99);

      expect(reservationServiceMock.search).toHaveBeenCalledWith(jasmine.objectContaining({
        accountId: 99
      }));
    });
  });

  describe('Navigation & Detail Paths', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should navigate to view detail page', () => {
      component.openDetailPage(mockReservations[0]);
      expect(routerMock.navigate).toHaveBeenCalledWith(['/staff/reservation/view', 1]);
    });

    it('should navigate to edit page', () => {
      component.openEditPage(mockReservations[0]);
      expect(routerMock.navigate).toHaveBeenCalledWith(['/staff/reservation/edit', 1]);
    });
  });

  describe('Delete Operations', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should open delete dialog and set state variables', () => {
      component.openDeleteDialog(mockReservations[0]);
      expect(component.reservationToDelete).toEqual(mockReservations[0]);
      expect(component.deleteError).toBeUndefined();
    });

    it('should cancel delete and clear variables', () => {
      component.reservationToDelete = mockReservations[0];
      component.deleteError = 'Some Error';
      component.deleteLoading = true;

      component.cancelDelete();

      expect(component.reservationToDelete).toBeUndefined();
      expect(component.deleteError).toBeUndefined();
      expect(component.deleteLoading).toBeFalse();
    });

    it('should do nothing on confirmDelete if no reservation to delete is selected', () => {
      component.reservationToDelete = undefined;
      component.confirmDelete();
      expect(reservationServiceMock.delete).not.toHaveBeenCalled();
    });

    it('should successfully delete reservation, splice it from array and show success toast', () => {
      component.reservations = [...mockReservations];
      component.reservationToDelete = mockReservations[0];
      reservationServiceMock.delete.and.returnValue(of(void 0));

      const translateService = TestBed.inject(TranslateService);
      spyOn(translateService, 'instant').and.returnValue('Deleted Success');

      component.confirmDelete();

      expect(reservationServiceMock.delete).toHaveBeenCalledWith(1);
      expect(component.reservations.length).toBe(2);
      expect(component.reservations.find(r => r.id === 1)).toBeUndefined();
      expect(toastrServiceMock.success).toHaveBeenCalledWith('Deleted Success');
    });

    it('should handle server error on delete and set deleteError message', () => {
      spyOn(console, 'error');
      component.reservationToDelete = mockReservations[0];
      reservationServiceMock.delete.and.returnValue(throwError(() => ({ error: { message: 'Cannot delete active item' } })));

      component.confirmDelete();

      expect(component.deleteError).toBe('Cannot delete active item');
      expect(component.deleteLoading).toBeFalse();
    });
  });

  describe('Filter Resetting', () => {
    it('should reset all inputs, criteria, results and the slider on clearAllFilters()', () => {
      fixture.detectChanges();
      component.dateFilter = '2026-05-05';
      component.timeFilter = '14:00';
      component.statusFilter = ReservationStatus.CANCELLED;
      component.showPastReservations = true;
      component.customerSearchCriteria = { firstName: 'A', lastName: 'B', email: 'C', userName: 'D' };
      component.foundCustomers = [mockCustomerResponse];
      component.selectedCustomerAccount = mockCustomerResponse;
      component.selectedCustomerId = 99;

      reservationServiceMock.search.calls.reset();
      component.clearAllFilters();

      expect(component.dateFilter).toBe('');
      expect(component.timeFilter).toBe('');
      expect(component.statusFilter).toBeNull();
      expect(component.showPastReservations).toBeFalse();
      expect(component.customerSearchCriteria).toEqual({ firstName: '', lastName: '', email: '', userName: '' });
      expect(component.foundCustomers).toEqual([]);
      expect(component.selectedCustomerAccount).toBeNull();
      expect(component.selectedCustomerId).toBeNull();
      expect(reservationServiceMock.search).toHaveBeenCalled();
    });
  });

  describe('Pagination & Helper Methods', () => {
    beforeEach(() => {
      fixture.detectChanges();
      component.reservations = Array.from({ length: 12 }).map((_, i) => ({
        id: i + 1,
        customerProfileId: 200 + i,
        customerName: `User ${i}`,
        accountId: 10,
        pickUpTime: '10:00:00',
        startDate: '2026-02-15',
        endDate: '2026-02-20',
        confirmationEmailSent: true,
        items: [],
        reservationStatus: ReservationStatus.CREATED
      } as ReservationDetail));
      component.itemLimit = 5;
      component.currentPage = 1;
    });

    it('should calculate correct indices for page 1', () => {
      expect(component.startIndex).toBe(1);
      expect(component.endIndex).toBe(5);
    });

    it('should calculate correct indices for page 2', () => {
      component.currentPage = 2;
      expect(component.startIndex).toBe(6);
      expect(component.endIndex).toBe(10);
    });

    it('should cap endIndex to list length on the last page', () => {
      component.currentPage = 3;
      expect(component.startIndex).toBe(11);
      expect(component.endIndex).toBe(12);
    });

    it('should step forward and backward correctly via pagination functions', () => {
      component.nextPage();
      expect(component.currentPage).toBe(2);

      component.previousPage();
      expect(component.currentPage).toBe(1);

      component.previousPage();
      expect(component.currentPage).toBe(1);
    });

    it('should not page forward if at the end of data', () => {
      component.currentPage = 3;
      component.nextPage();
      expect(component.currentPage).toBe(3);
    });

    it('should jump directly to the last page', () => {
      component.goToLastPage();
      expect(component.currentPage).toBe(3);
    });

    it('should auto-decrement currentPage if items on the last page are completely deleted', () => {
      component.currentPage = 3;
      component.reservationToDelete = component.reservations[11];
      reservationServiceMock.delete.and.returnValue(of(void 0));

      component.confirmDelete();
      expect(component.currentPage).toBe(3);

      component.reservationToDelete = component.reservations[10];
      component.confirmDelete();

      expect(component.currentPage).toBe(2);
    });
  });
});
