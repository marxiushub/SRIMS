import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateModule, TranslateService} from "@ngx-translate/core";
import {of, throwError} from "rxjs";
import {Router} from "@angular/router";

import {CustomerProfileComponent} from './customer-profile.component';
import {CustomerProfileService} from '../../../services/customer-profile.service';
import {SkillLevel} from '../../../dtos/skilllevel';
import {CustomerProfile} from '../../../dtos/customer-profile';
import {ToastrModule, ToastrService} from 'ngx-toastr';
import {NgbCollapse} from '@ng-bootstrap/ng-bootstrap';

describe('CustomerProfileComponent', () => {
  let component: CustomerProfileComponent;
  let fixture: ComponentFixture<CustomerProfileComponent>;
  let customerProfileServiceMock: jasmine.SpyObj<CustomerProfileService>;
  let toastrServiceMock: jasmine.SpyObj<ToastrService>;

  const routerMock = {
    navigate: jasmine.createSpy("navigate"),
  };

  const testProfiles: CustomerProfile[] = [
    {
      id: 1,
      profileName: 'Profile 1',
      height: 180,
      weight: 75,
      shoeSize: 42,
      skillLevel: SkillLevel.BEGINNER,
      customerId: 1
    },
    {
      id: 2,
      profileName: 'Profile 2',
      height: 165,
      weight: 60,
      shoeSize: 38,
      skillLevel: SkillLevel.ADVANCED,
      customerId: 1
    }
  ];

  beforeEach(async () => {
    customerProfileServiceMock = jasmine.createSpyObj('CustomerProfileService', ['getCustomerProfiles', 'delete']);
    customerProfileServiceMock.getCustomerProfiles.and.returnValue(of(testProfiles));
    customerProfileServiceMock.delete.and.returnValue(of(void 0));
    toastrServiceMock = jasmine.createSpyObj('ToastrService', ['success', 'error', 'warning', 'info']);

    routerMock.navigate.calls.reset();

    await TestBed.configureTestingModule({
      declarations: [CustomerProfileComponent],
      imports: [NgbCollapse, TranslateModule.forRoot(), ToastrModule.forRoot()],
      providers: [
        {provide: CustomerProfileService, useValue: customerProfileServiceMock},
        {provide: Router, useValue: routerMock},
        {provide: ToastrService, useValue: toastrServiceMock}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(CustomerProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load profiles on init', () => {
    expect(customerProfileServiceMock.getCustomerProfiles).toHaveBeenCalled();
    expect(component.profiles.length).toBe(2);
    expect(component.profiles).toEqual(testProfiles);
  });

  it('should navigate to create page', () => {
    component.openCreatePage();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/customer/profiles/create']);
  });

  it('should navigate to edit page', () => {
    component.openEditPage(testProfiles[0]);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/customer/profiles/edit', 1]);
  });

  it('should open delete dialog', () => {
    component.openDeleteDialog(testProfiles[0]);
    expect(component.profileToDelete).toEqual(testProfiles[0]);
    expect(component.deleteError).toBeUndefined();
  });

  it('should cancel delete', () => {
    component.profileToDelete = testProfiles[0];
    component.deleteError = 'Error';
    component.deleteLoading = true;

    component.cancelDelete();

    expect(component.profileToDelete).toBeUndefined();
    expect(component.deleteError).toBeUndefined();
    expect(component.deleteLoading).toBeFalse();
  });

  it('should set an error when delete fails', () => {
    spyOn(console, 'error');
    customerProfileServiceMock.delete.and.returnValue(throwError(() => new Error('Delete failed')));
    component.profileToDelete = testProfiles[0];

    component.confirmDelete();

    expect(component.deleteError).toBe('Delete failed');
    expect(component.deleteLoading).toBeFalse();
    expect(component.profileToDelete).toEqual(testProfiles[0]);
  });

  it('should confirm delete, remove profile, and show success notification', () => {
    component.profiles = [...testProfiles];
    component.profileToDelete = testProfiles[0];

    const translateService = TestBed.inject(TranslateService);
    spyOn(translateService, 'instant').and.returnValue('Profile deleted');

    component.confirmDelete();

    expect(customerProfileServiceMock.delete).toHaveBeenCalledWith(1);
    expect(component.profiles.length).toBe(1);
    expect(component.profiles[0].id).toBe(2);
    expect(component.profileToDelete).toBeUndefined();
    expect(component.deleteLoading).toBeFalse();
    expect(toastrServiceMock.success).toHaveBeenCalledWith('Profile deleted');
  });
});
