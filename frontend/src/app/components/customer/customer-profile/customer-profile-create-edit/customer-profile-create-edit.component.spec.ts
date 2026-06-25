import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule, NgForm} from "@angular/forms";
import {TranslateModule, TranslateService} from "@ngx-translate/core";
import {of, throwError} from "rxjs";
import {ActivatedRoute, convertToParamMap, Router} from "@angular/router";

import {CustomerProfileCreateEditComponent, ProfileCreateEditMode} from './customer-profile-create-edit.component';
import {CustomerProfile} from "../../../../dtos/customer-profile";
import {CustomerProfileService} from "../../../../services/customer-profile.service";
import {SkillLevel} from "../../../../dtos/skilllevel";
import {ToastrModule, ToastrService} from 'ngx-toastr';
import {NgbCollapse} from '@ng-bootstrap/ng-bootstrap';

describe('CustomerProfileCreateEditComponent', () => {
  let component: CustomerProfileCreateEditComponent;
  let fixture: ComponentFixture<CustomerProfileCreateEditComponent>;
  let customerProfileServiceMock: jasmine.SpyObj<CustomerProfileService>;
  let toastrServiceMock: jasmine.SpyObj<ToastrService>;

  const routerMock = {
    navigate: jasmine.createSpy("navigate"),
  };

  const activatedRouteMock = {
    snapshot: {
      paramMap: convertToParamMap({})
    }
  };

  const mockProfile: CustomerProfile = {
    id: 1,
    profileName: 'Loaded Profile',
    height: 175,
    weight: 70,
    shoeSize: 42,
    skillLevel: SkillLevel.INTERMEDIATE,
    customerId: 1,
  };

  beforeEach(async () => {
    customerProfileServiceMock = jasmine.createSpyObj('CustomerProfileService', ['create', 'update', 'getById']);
    customerProfileServiceMock.create.and.returnValue(of({} as any));
    customerProfileServiceMock.update.and.returnValue(of({} as any));
    customerProfileServiceMock.getById.and.returnValue(of(mockProfile));
    toastrServiceMock = jasmine.createSpyObj('ToastrService', ['success', 'error', 'warning', 'info']);

    activatedRouteMock.snapshot.paramMap = convertToParamMap({});
    routerMock.navigate.calls.reset();

    await TestBed.configureTestingModule({
      declarations: [CustomerProfileCreateEditComponent],
      imports: [FormsModule, NgbCollapse, TranslateModule.forRoot(), ToastrModule.forRoot()],
      providers: [
        {provide: CustomerProfileService, useValue: customerProfileServiceMock},
        {provide: Router, useValue: routerMock},
        {provide: ActivatedRoute, useValue: activatedRouteMock},
        {provide: ToastrService, useValue: toastrServiceMock}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(CustomerProfileCreateEditComponent);
    component = fixture.componentInstance;
  });

  it('should create in create mode by default', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component).toBeTruthy();
    expect(component.mode).toBe(ProfileCreateEditMode.create);
    expect(customerProfileServiceMock.getById).not.toHaveBeenCalled();
  });

  it('should initialize in edit mode and load profile when id is provided in route', async () => {
    activatedRouteMock.snapshot.paramMap = convertToParamMap({id: '1'});
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.mode).toBe(ProfileCreateEditMode.edit);
    expect(component.profileId).toBe(1);
    expect(customerProfileServiceMock.getById).toHaveBeenCalledWith(1);
    expect(component.profile).toEqual({
      profileName: mockProfile.profileName,
      height: mockProfile.height,
      weight: mockProfile.weight,
      shoeSize: mockProfile.shoeSize,
      skillLevel: mockProfile.skillLevel
    });
  });

  it('should call create service and navigate on submit in create mode', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.profile = {
      profileName: 'New Profile',
      height: 180,
      weight: 80,
      shoeSize: 44,
      skillLevel: SkillLevel.BEGINNER
    };

    component.onSubmit();

    expect(customerProfileServiceMock.create).toHaveBeenCalledWith(component.profile);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/customer/profiles']);
    expect(component.loading).toBeFalse();
    expect(component.error).toBeFalse();
  });

  it('should set error when create request fails', async () => {
    spyOn(console, 'error');
    fixture.detectChanges();
    await fixture.whenStable();

    customerProfileServiceMock.create.and.returnValue(throwError(() => new Error('Create failed')));
    component.onSubmit();

    expect(component.error).toBeTrue();
    expect(component.loading).toBeFalse();
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('should call update service and navigate on submit in edit mode', async () => {
    activatedRouteMock.snapshot.paramMap = convertToParamMap({id: '1'});
    fixture.detectChanges();
    await fixture.whenStable();

    component.profile.profileName = 'Updated Profile';
    component.onSubmit();

    expect(customerProfileServiceMock.update).toHaveBeenCalledWith(1, component.profile);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/customer/profiles']);
  });

  it('should set error when update request fails', async () => {
    spyOn(console, 'error');
    activatedRouteMock.snapshot.paramMap = convertToParamMap({id: '1'});
    fixture.detectChanges();
    await fixture.whenStable();

    customerProfileServiceMock.update.and.returnValue(throwError(() => new Error('Update failed')));
    component.onSubmit();

    expect(component.error).toBeTrue();
    expect(component.loading).toBeFalse();
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('should not call service if form is invalid', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    const mockForm = {
      invalid: true,
      control: {
        markAllAsTouched: jasmine.createSpy('markAllAsTouched')
      }
    } as unknown as NgForm;

    component.onSubmit(mockForm);

    expect(customerProfileServiceMock.create).not.toHaveBeenCalled();
    expect(customerProfileServiceMock.update).not.toHaveBeenCalled();
    expect(component.submitted).toBeTrue();
  });

  it('should call create service, navigate on submit in create mode, and show success notification', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.profile = {
      profileName: 'New Profile',
      height: 180,
      weight: 80,
      shoeSize: 44,
      skillLevel: SkillLevel.BEGINNER
    };

    const translateService = TestBed.inject(TranslateService);
    spyOn(translateService, 'instant').and.returnValue('Profile successfully created');

    component.onSubmit();

    expect(customerProfileServiceMock.create).toHaveBeenCalledWith(component.profile);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/customer/profiles']);
    expect(component.loading).toBeFalse();
    expect(component.error).toBeFalse();

    expect(toastrServiceMock.success).toHaveBeenCalledWith('Profile successfully created');
  });

  it('should call update service, navigate on submit in edit mode, and show success notification', async () => {
    activatedRouteMock.snapshot.paramMap = convertToParamMap({id: '1'});
    fixture.detectChanges();
    await fixture.whenStable();

    component.profile.profileName = 'Updated Profile';

    const translateService = TestBed.inject(TranslateService);
    spyOn(translateService, 'instant').and.returnValue('Profile successfully updated');

    component.onSubmit();

    expect(customerProfileServiceMock.update).toHaveBeenCalledWith(1, component.profile);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/customer/profiles']);

    expect(toastrServiceMock.success).toHaveBeenCalledWith('Profile successfully updated');
  });
});
