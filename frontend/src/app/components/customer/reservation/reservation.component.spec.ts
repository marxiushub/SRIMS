import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateModule, TranslateService} from "@ngx-translate/core";
import {of, throwError} from "rxjs";
import {provideHttpClient} from "@angular/common/http";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {RouterModule} from "@angular/router";
import {FormsModule} from '@angular/forms';

import {ReservationComponent} from "./reservation.component";
import {ReservationService} from "../../../services/reservation.service";
import {CustomerProfileService} from "../../../services/customer-profile.service";
import {ReservationDetail} from '../../../dtos/reservation-detail';
import {CustomerProfile} from '../../../dtos/customer-profile';
import {SkillLevel} from "../../../dtos/skilllevel";
import {ToastrModule, ToastrService} from 'ngx-toastr';
import {ReservationStatus} from "../../../dtos/reservationstatus";

// AI-assisted: Code generated with Google Gemini and adapted
describe('ReservationComponent', () => {
  let component: ReservationComponent;
  let fixture: ComponentFixture<ReservationComponent>;
  let reservationServiceMock: jasmine.SpyObj<ReservationService>;
  let customerProfileServiceMock: jasmine.SpyObj<CustomerProfileService>;
  let toastrServiceMock: jasmine.SpyObj<ToastrService>;

  const testProfiles: CustomerProfile[] = [
    {
      id: 1,
      customerId: 1,
      profileName: 'Max Mustermann',
      height: 180,
      weight: 75,
      shoeSize: 42,
      skillLevel: SkillLevel.INTERMEDIATE
    },
    {
      id: 2,
      customerId: 2,
      profileName: 'Erika Musterfrau',
      height: 165,
      weight: 55,
      shoeSize: 38,
      skillLevel: SkillLevel.ADVANCED
    }
  ];

  const testReservations: ReservationDetail[] = [
    {
      id: 100,
      customerProfileId: 10,
      accountId: 1,
      customerName: 'Max Mustermann',
      pickUpTime: '10:00:00',
      startDate: '2026-12-24',
      endDate: '2026-12-31',
      confirmationEmailSent: true,
      items: [{id: 1, model: 'Ski Alpha', price: 25} as any],
      reservationStatus: ReservationStatus.CREATED
    },
    {
      id: 200,
      customerProfileId: 20,
      accountId: 1,
      customerName: 'Erika Musterfrau',
      pickUpTime: '08:30:00',
      startDate: '2026-02-15',
      endDate: '2026-02-20',
      confirmationEmailSent: false,
      items: [],
      reservationStatus: ReservationStatus.PICKED_UP
    }
  ];

  beforeEach(async () => {
    reservationServiceMock = jasmine.createSpyObj('ReservationService', ['search', 'delete']);
    customerProfileServiceMock = jasmine.createSpyObj('CustomerProfileService', ['getCustomerProfiles']);

    reservationServiceMock.search.and.returnValue(of([]));
    customerProfileServiceMock.getCustomerProfiles.and.returnValue(of(testProfiles));
    reservationServiceMock.delete.and.returnValue(of(void 0));
    toastrServiceMock = jasmine.createSpyObj('ToastrService', ['success', 'error', 'warning', 'info']);

    await TestBed.configureTestingModule({
      declarations: [ReservationComponent],
      imports: [
        RouterModule.forRoot([]),
        TranslateModule.forRoot(),
        ToastrModule.forRoot(),
        FormsModule
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: ReservationService, useValue: reservationServiceMock},
        {provide: CustomerProfileService, useValue: customerProfileServiceMock},
        {provide: ToastrService, useValue: toastrServiceMock}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReservationComponent);
    component = fixture.componentInstance;
    reservationServiceMock.search.and.returnValue(of([]));
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load customer profiles for dropdown on init', () => {
    expect(customerProfileServiceMock.getCustomerProfiles).toHaveBeenCalledWith();
    expect(component.customerProfiles).toEqual(testProfiles);
  });

  it('should open delete dialog for selected reservation', () => {
    component.openDeleteDialog(testReservations[0]);

    expect(component.reservationToDelete).toEqual(testReservations[0]);
    expect(component.deleteError).toBeUndefined();
  });

  it('should cancel delete and clear delete state', () => {
    component.reservationToDelete = testReservations[0];
    component.deleteError = 'Error message';
    component.deleteLoading = true;

    component.cancelDelete();

    expect(component.reservationToDelete).toBeUndefined();
    expect(component.deleteError).toBeUndefined();
    expect(component.deleteLoading).toBeFalse();
  });

  it('should call delete service and remove reservation from list, and show success notification', () => {
    component.reservations = [...testReservations];
    component.reservationToDelete = testReservations[0];

    const translateService = TestBed.inject(TranslateService);
    spyOn(translateService, 'instant').and.returnValue('Reservation deleted');

    component.confirmDelete();

    expect(reservationServiceMock.delete).toHaveBeenCalledWith(100); // ID of first Reservation
    expect(component.reservations.length).toBe(1);
    expect(component.reservations[0].id).toBe(200);
    expect(component.reservationToDelete).toBeUndefined();
    expect(component.deleteLoading).toBeFalse();
    expect(toastrServiceMock.success).toHaveBeenCalledWith('Reservation deleted');
  });

  it('should call search twice with CREATED and PICKED_UP status when showPastReservations is false', () => {
    component.profileFilter = 10;
    component.dateFilter = '2026-12-24';
    component.timeFilter = '10:00';
    component.showPastReservations = false;

    reservationServiceMock.search.and.returnValues(of([testReservations[0]]), of([testReservations[1]]));

    component.searchReservations();

    expect(reservationServiceMock.search).toHaveBeenCalledWith({
      accountId: 1,
      customerProfileId: 10,
      pickUpTime: '10:00:00',
      startDate: '2026-12-24',
      reservationStatus: ReservationStatus.CREATED
    });

    expect(reservationServiceMock.search).toHaveBeenCalledWith({
      accountId: 1,
      customerProfileId: 10,
      pickUpTime: '10:00:00',
      startDate: '2026-12-24',
      reservationStatus: ReservationStatus.PICKED_UP
    });

    expect(component.reservations).toEqual([...testReservations]);
    expect(component.loading).toBeFalse();
  });

  it('should call search only once without status filter when showPastReservations is true', () => {
    component.profileFilter = 10;
    component.dateFilter = '2026-12-24';
    component.timeFilter = '10:00';
    component.showPastReservations = true;

    reservationServiceMock.search.calls.reset();
    reservationServiceMock.search.and.returnValue(of(testReservations));

    component.searchReservations();

    expect(reservationServiceMock.search).toHaveBeenCalledOnceWith({
      accountId: 1,
      customerProfileId: 10,
      pickUpTime: '10:00:00',
      startDate: '2026-12-24'
    });
    expect(component.reservations).toEqual(testReservations);
    expect(component.loading).toBeFalse();
  });

  it('should call search twice with mapped filters and status when showPastReservations is false', () => {
    component.profileFilter = 10;
    component.dateFilter = '2026-12-24';
    component.timeFilter = '10:00';
    component.showPastReservations = false;

    reservationServiceMock.search.and.returnValues(of([]), of([]));

    component.searchReservations();

    expect(reservationServiceMock.search).toHaveBeenCalledWith({
      accountId: 1,
      customerProfileId: 10,
      pickUpTime: '10:00:00',
      startDate: '2026-12-24',
      reservationStatus: ReservationStatus.CREATED
    });

    expect(reservationServiceMock.search).toHaveBeenCalledWith({
      accountId: 1,
      customerProfileId: 10,
      pickUpTime: '10:00:00',
      startDate: '2026-12-24',
      reservationStatus: ReservationStatus.PICKED_UP
    });
    expect(component.loading).toBeFalse();
  });

  it('should call search once with mapped filters and NO status when showPastReservations is true', () => {
    component.profileFilter = 10;
    component.dateFilter = '2026-12-24';
    component.timeFilter = '10:00';
    component.showPastReservations = true;

    reservationServiceMock.search.calls.reset();
    reservationServiceMock.search.and.returnValue(of([]));

    component.searchReservations();

    expect(reservationServiceMock.search).toHaveBeenCalledOnceWith({
      accountId: 1,
      customerProfileId: 10,
      pickUpTime: '10:00:00',
      startDate: '2026-12-24'
    });
    expect(component.loading).toBeFalse();
  });

  it('should pass undefined for empty filters except hardcoded accountId', () => {
    component.profileFilter = null;
    component.dateFilter = '';
    component.timeFilter = '';
    component.showPastReservations = false;

    reservationServiceMock.search.and.returnValue(of([]));

    component.searchReservations();

    expect(reservationServiceMock.search).toHaveBeenCalledWith({
      accountId: 1,
      customerProfileId: undefined,
      pickUpTime: undefined,
      startDate: undefined,
      reservationStatus: ReservationStatus.CREATED
    });
    expect(reservationServiceMock.search).toHaveBeenCalledWith({
      accountId: 1,
      customerProfileId: undefined,
      pickUpTime: undefined,
      startDate: undefined,
      reservationStatus: ReservationStatus.PICKED_UP
    });
  });

  it('should clear filters and reload reservations for the account', () => {
    component.profileFilter = 20;
    component.dateFilter = '2026-02-15';
    component.timeFilter = '08:30';
    component.showPastReservations = true;

    reservationServiceMock.search.calls.reset();
    reservationServiceMock.search.and.returnValue(of(testReservations));

    component.clearFilters();

    expect(component.profileFilter).toBeNull();
    expect(component.dateFilter).toBe('');
    expect(component.timeFilter).toBe('');
    expect(component.showPastReservations).toBeFalse();

    expect(reservationServiceMock.search).toHaveBeenCalledWith({
      accountId: 1,
      customerProfileId: undefined,
      pickUpTime: undefined,
      startDate: undefined,
      reservationStatus: ReservationStatus.CREATED
    });
    expect(reservationServiceMock.search).toHaveBeenCalledWith({
      accountId: 1,
      customerProfileId: undefined,
      pickUpTime: undefined,
      startDate: undefined,
      reservationStatus: ReservationStatus.PICKED_UP
    });
  });

  it('should set loading false when search fails', () => {
    spyOn(console, 'error');

    component.loading = true;
    reservationServiceMock.search.and.returnValue(
      throwError(() => new Error('Search failed'))
    );

    component.searchReservations();

    expect(component.loading).toBeFalse();
  });

  describe('Pagination', () => {
    beforeEach(() => {
      const manyReservations = Array.from({length: 12}).map((_, i) => ({
        id: i + 1,
        customerProfileId: 1,
        accountId: 1,
        customerName: `Customer ${i}`,
        pickUpTime: '10:00:00',
        startDate: '2026-12-24',
        endDate: '2026-12-27',
        confirmationEmailSent: false,
        items: [],
        reservationStatus: ReservationStatus.CREATED
      })) as ReservationDetail[];

      component.reservations = manyReservations;
      component.itemLimit = 5;
      component.currentPage = 1;
    });

    it('should calculate correct startIndex and endIndex for page 1', () => {
      expect(component.startIndex).toBe(1);
      expect(component.endIndex).toBe(5);
    });

    it('should calculate correct startIndex and endIndex for the last page', () => {
      component.goToLastPage();
      expect(component.currentPage).toBe(3);
      expect(component.startIndex).toBe(11);
      expect(component.endIndex).toBe(12);
    });

    it('should navigate between pages correctly', () => {
      component.nextPage();
      expect(component.currentPage).toBe(2);

      component.previousPage();
      expect(component.currentPage).toBe(1);

      component.previousPage();
      expect(component.currentPage).toBe(1);
    });

    it('should go to previous page if the last items of the current page are deleted', () => {
      component.goToLastPage();

      component.reservationToDelete = component.reservations[11];
      component.confirmDelete();

      component.reservationToDelete = component.reservations[10];
      component.confirmDelete();

      expect(component.currentPage).toBe(2);
    });
  });
});
