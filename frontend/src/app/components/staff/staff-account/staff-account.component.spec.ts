import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {Router} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {ToastrModule, ToastrService} from 'ngx-toastr';
import {of, throwError} from 'rxjs';
import {StaffAccountComponent} from './staff-account.component';
import {AuthService} from '../../../services/auth.service';
import {StaffService} from '../../../services/staff.service';
import {StaffSearchResponse} from '../../../dtos/staff-search-response';
import {UserType} from '../../../dtos/usertype';

describe('StaffAccountComponent', () => {
  let component: StaffAccountComponent;
  let fixture: ComponentFixture<StaffAccountComponent>;
  let authService: AuthService;
  let staffService: StaffService;
  let router: Router;
  let notification: ToastrService;

  const mockAccount: StaffSearchResponse = {
    id: 1,
    userName: 'admin',
    email: 'admin@srims.at',
    userType: UserType.STAFF
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [StaffAccountComponent],
      imports: [RouterTestingModule, ReactiveFormsModule, TranslateModule.forRoot(), ToastrModule.forRoot()],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StaffAccountComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    staffService = TestBed.inject(StaffService);
    router = TestBed.inject(Router);
    notification = TestBed.inject(ToastrService);
  });

  it('should create', () => {
    spyOn(authService, 'getUserId').and.returnValue(1);
    spyOn(staffService, 'getById').and.returnValue(of(mockAccount));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load the account data on init using the id from the auth service', () => {
    spyOn(authService, 'getUserId').and.returnValue(1);
    spyOn(staffService, 'getById').and.returnValue(of(mockAccount));

    fixture.detectChanges();

    expect(staffService.getById).toHaveBeenCalledWith(1);
    expect(component.account).toEqual(mockAccount);
    expect(component.loading).toBeFalse();
    expect(component.loadError).toBeFalse();
  });

  it('should set loadError when no user id is available', () => {
    spyOn(authService, 'getUserId').and.returnValue(null);
    spyOn(staffService, 'getById');

    fixture.detectChanges();

    expect(staffService.getById).not.toHaveBeenCalled();
    expect(component.loadError).toBeTrue();
  });

  it('should set loadError when loading the account fails', () => {
    spyOn(authService, 'getUserId').and.returnValue(1);
    spyOn(staffService, 'getById').and.returnValue(throwError(() => ({error: {message: 'Not found'}})));

    fixture.detectChanges();

    expect(component.loadError).toBeTrue();
    expect(component.loading).toBeFalse();
  });

  describe('password change form', () => {
    beforeEach(() => {
      spyOn(authService, 'getUserId').and.returnValue(1);
      spyOn(staffService, 'getById').and.returnValue(of(mockAccount));
      fixture.detectChanges();
    });

    it('should require oldPassword and a valid newPassword', () => {
      component.passwordForm.controls.oldPassword.setValue('');
      component.passwordForm.controls.newPassword.setValue('');
      expect(component.passwordForm.valid).toBeFalse();

      component.passwordForm.controls.oldPassword.setValue('oldPass1!');
      component.passwordForm.controls.newPassword.setValue('short');
      expect(component.passwordForm.valid).toBeFalse();

      component.passwordForm.controls.newPassword.setValue('ValidNewPass1!');
      expect(component.passwordForm.valid).toBeTrue();
    });

    it('should call staffService.changePassword and reset the form on success', () => {
      spyOn(staffService, 'changePassword').and.returnValue(of(mockAccount));
      spyOn(notification, 'success');

      component.passwordForm.controls.oldPassword.setValue('oldPass1!');
      component.passwordForm.controls.newPassword.setValue('ValidNewPass1!');
      component.onSubmitPasswordChange();

      expect(staffService.changePassword).toHaveBeenCalledWith(1, {
        oldPassword: 'oldPass1!',
        newPassword: 'ValidNewPass1!'
      });
      expect(notification.success).toHaveBeenCalled();
      expect(component.passwordForm.controls.oldPassword.value).toBe('');
      expect(component.passwordForm.controls.newPassword.value).toBe('');
    });

    it('should show an error notification when the password change fails', () => {
      spyOn(staffService, 'changePassword').and.returnValue(throwError(() => ({error: {message: 'Old password incorrect'}})));
      spyOn(notification, 'error');

      component.passwordForm.controls.oldPassword.setValue('wrongOld1!');
      component.passwordForm.controls.newPassword.setValue('ValidNewPass1!');
      component.onSubmitPasswordChange();

      expect(notification.error).toHaveBeenCalled();
    });

    it('should not call changePassword when the form is invalid', () => {
      spyOn(staffService, 'changePassword');

      component.passwordForm.controls.oldPassword.setValue('');
      component.passwordForm.controls.newPassword.setValue('');
      component.onSubmitPasswordChange();

      expect(staffService.changePassword).not.toHaveBeenCalled();
    });
  });

  it('should navigate back to /staff when back() is called', () => {
    spyOn(authService, 'getUserId').and.returnValue(1);
    spyOn(staffService, 'getById').and.returnValue(of(mockAccount));
    fixture.detectChanges();

    spyOn(router, 'navigate');
    component.back();

    expect(router.navigate).toHaveBeenCalledWith(['/staff']);
  });
});
