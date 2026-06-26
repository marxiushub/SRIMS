import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReservationViewComponent} from './reservation-view.component';
import {ReservationService} from '../../../../services/reservation.service';
import {EquipmentType} from "../../../../dtos/equipmenttype";
import {RentalStatus} from "../../../../dtos/rentalstatus";
import {SkillLevel} from "../../../../dtos/skilllevel";
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {TranslateModule} from '@ngx-translate/core';
import {ActivatedRoute, Router} from '@angular/router';
import {of, throwError} from 'rxjs';
import {ToastrService} from 'ngx-toastr';
import {registerLocaleData} from '@angular/common';
import localeDe from '@angular/common/locales/de';
import {NgbCollapse, NgbModule} from '@ng-bootstrap/ng-bootstrap';

registerLocaleData(localeDe, 'de');

// AI-assisted: Code generated with Google Gemini and adapted to fit project
describe('ReservationViewComponent', () => {
  let component: ReservationViewComponent;
  let fixture: ComponentFixture<ReservationViewComponent>;

  let reservationServiceSpy: jasmine.SpyObj<ReservationService>;
  let router: Router;
  let toastrSpy: jasmine.SpyObj<ToastrService>;

  const mockReservation = {
    id: 1,
    customerProfileId: 10,
    accountId: 2,
    customerName: 'Max Mustermann',
    pickUpTime: '09:00:00',
    startDate: '2026-12-24',
    endDate: '2026-12-26',
    reservationStatus: 'CREATED',
    confirmationEmailSent: true,
    items: [
      {
        id: 101,
        model: 'Pro Ski 2026',
        equipmentType: EquipmentType.SKI,
        targetSkillLevel: SkillLevel.ADVANCED,
        price: 35.0,
        barcodeId: 'BC-101',
        status: RentalStatus.RESERVED
      }
    ]
  } as any;

  beforeEach(waitForAsync(() => {
    const resSpy = jasmine.createSpyObj('ReservationService', ['getById', 'delete']);
    const toastSpy = jasmine.createSpyObj('ToastrService', ['success', 'error']);

    TestBed.configureTestingModule({
      declarations: [ReservationViewComponent],
      imports: [
        RouterTestingModule,
        NgbModule,
        NgbCollapse,
        TranslateModule.forRoot()
      ],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        {provide: ReservationService, useValue: resSpy},
        {provide: ToastrService, useValue: toastSpy},
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
      ]
    }).compileComponents();

    reservationServiceSpy = TestBed.inject(ReservationService) as jasmine.SpyObj<ReservationService>;
    toastrSpy = TestBed.inject(ToastrService) as jasmine.SpyObj<ToastrService>;
    router = TestBed.inject(Router);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReservationViewComponent);
    component = fixture.componentInstance;

    reservationServiceSpy.getById.and.returnValue(of(mockReservation));

    fixture.detectChanges();
  });

  it('should create the view component', () => {
    expect(component).toBeTruthy();
  });

  it('should load reservation details successfully on init', () => {
    expect(reservationServiceSpy.getById).toHaveBeenCalledWith(1);
    expect(component.reservation).toBeTruthy();
    expect(component.reservation?.customerName).toBe('Max Mustermann');
    expect(component.loading).toBeFalse();
    expect(component.error).toBeUndefined();
  });

  it('should handle errors when backend call fails on init', () => {
    spyOn(console, 'error');
    reservationServiceSpy.getById.and.returnValue(throwError(() => new Error('Backend failure')));

    component.reservation = undefined;

    component.ngOnInit();

    expect(component.loading).toBeFalse();
    expect(component.reservation).toBeUndefined();
    expect(component.error).toBe('Backend failure');
  });

  it('should safely extract selected equipment through the getter', () => {
    expect(component.selectedEquipment.length).toBe(1);
    expect(component.selectedEquipment[0].model).toBe('Pro Ski 2026');
  });

  it('should clean up and cut seconds from pickup time string', () => {
    const formatted = component.getFormattedTime('14:30:00');
    expect(formatted).toBe('14:30');

    const shortFormatted = component.getFormattedTime('09:15');
    expect(shortFormatted).toBe('09:15');
  });

  it('should navigate back to list view on backToList()', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.backToList();
    expect(navigateSpy).toHaveBeenCalledWith(['/customer/reservation']);
  });

  it('should navigate to edit view on openEditPage()', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.openEditPage();
    expect(navigateSpy).toHaveBeenCalledWith(['/customer/reservation/edit', 1]);
  });

  it('should manage modal visibility flags correctly when calling dialog functions', () => {
    expect(component.showDeleteModal).toBeFalse();

    component.openDeleteDialog();
    expect(component.showDeleteModal).toBeTrue();
    expect(component.deleteError).toBeUndefined();

    component.cancelDelete();
    expect(component.showDeleteModal).toBeFalse();
  });

  it('should successfully execute delete and navigate away', () => {
    const navigateSpy = spyOn(router, 'navigate');
    reservationServiceSpy.delete.and.returnValue(of(null as any));
    component.reservation = mockReservation;

    component.openDeleteDialog();
    component.confirmDelete();

    expect(reservationServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(component.deleteLoading).toBeFalse();
    expect(component.showDeleteModal).toBeFalse();
    expect(toastrSpy.success).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/customer/reservation']);
  });

  it('should display error message if delete service call fails', () => {
    spyOn(console, 'error');
    const errorResponse = {error: {message: 'Cannot delete active reservation'}};
    reservationServiceSpy.delete.and.returnValue(throwError(() => errorResponse));
    component.reservation = mockReservation;

    component.openDeleteDialog();
    component.confirmDelete();

    expect(reservationServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(component.deleteLoading).toBeFalse();
    expect(component.showDeleteModal).toBeTrue(); // Modal bleibt offen
    expect(component.deleteError).toBe('Cannot delete active reservation');
  });
});
