import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {Router} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {ToastrModule, ToastrService} from 'ngx-toastr';
import {of, throwError} from 'rxjs';
import {CustomerAccountComponent} from './customer-account.component';
import {AuthService} from '../../../services/auth.service';
import {CustomerService} from '../../../services/customer.service';
import {CustomerSearchResponse} from '../../../dtos/customer-search-response';
import {UserType} from '../../../dtos/usertype';

describe('CustomerAccountComponent', () => {
  let component: CustomerAccountComponent;
  let fixture: ComponentFixture<CustomerAccountComponent>;
  let authService: AuthService;
  let customerService: CustomerService;
  let router: Router;
  let notification: ToastrService;

  const mockAccount: CustomerSearchResponse = {
    id: 20,
    userName: 'jdoe',
    email: 'jdoe@example.com',
    userType: UserType.CUSTOMER,
    firstName: 'John',
    lastName: 'Doe',
    dateOfBirth: '1990-01-01'
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [CustomerAccountComponent],
      imports: [RouterTestingModule, ReactiveFormsModule, TranslateModule.forRoot(), ToastrModule.forRoot()],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomerAccountComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    customerService = TestBed.inject(CustomerService);
    router = TestBed.inject(Router);
    notification = TestBed.inject(ToastrService);
  });

  it('should create', () => {
    spyOn(authService, 'getUserId').and.returnValue(20);
    spyOn(customerService, 'getById').and.returnValue(of(mockAccount));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load the account data on init using the id from the auth service', () => {
    spyOn(authService, 'getUserId').and.returnValue(20);
    spyOn(customerService, 'getById').and.returnValue(of(mockAccount));

    fixture.detectChanges();

    expect(customerService.getById).toHaveBeenCalledWith(20);
    expect(component.account).toEqual(mockAccount);
    expect(component.loading).toBeFalse();
    expect(component.loadError).toBeFalse();
  });

  it('should set loadError when no user id is available', () => {
    spyOn(authService, 'getUserId').and.returnValue(null);
    spyOn(customerService, 'getById');

    fixture.detectChanges();

    expect(customerService.getById).not.toHaveBeenCalled();
    expect(component.loadError).toBeTrue();
  });

  it('should set loadError when loading the account fails', () => {
    spyOn(authService, 'getUserId').and.returnValue(20);
    spyOn(customerService, 'getById').and.returnValue(throwError(() => ({error: {message: 'Not found'}})));

    fixture.detectChanges();

    expect(component.loadError).toBeTrue();
    expect(component.loading).toBeFalse();
  });

  describe('password change form', () => {
    beforeEach(() => {
      spyOn(authService, 'getUserId').and.returnValue(20);
      spyOn(customerService, 'getById').and.returnValue(of(mockAccount));
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

    it('should call customerService.changePassword and reset the form on success', () => {
      spyOn(customerService, 'changePassword').and.returnValue(of(mockAccount));
      spyOn(notification, 'success');

      component.passwordForm.controls.oldPassword.setValue('oldPass1!');
      component.passwordForm.controls.newPassword.setValue('ValidNewPass1!');
      component.onSubmitPasswordChange();
      fixture.detectChanges();

      expect(customerService.changePassword).toHaveBeenCalledWith(20, {
        oldPassword: 'oldPass1!',
        newPassword: 'ValidNewPass1!'
      });
      expect(notification.success).toHaveBeenCalled();
      expect(component.passwordForm.controls.oldPassword.value).toBeNull();
      expect(component.passwordForm.controls.newPassword.value).toBeNull();
    });

    it('should show an error notification when the password change fails', () => {
      spyOn(customerService, 'changePassword').and.returnValue(throwError(() => ({error: {message: 'Old password incorrect'}})));
      spyOn(notification, 'error');

      component.passwordForm.controls.oldPassword.setValue('wrongOld1!');
      component.passwordForm.controls.newPassword.setValue('ValidNewPass1!');
      component.onSubmitPasswordChange();
      fixture.detectChanges();

      expect(notification.error).toHaveBeenCalled();
    });

  });

  it('should navigate back to /customer when back() is called', () => {
    spyOn(authService, 'getUserId').and.returnValue(20);
    spyOn(customerService, 'getById').and.returnValue(of(mockAccount));
    fixture.detectChanges();

    spyOn(router, 'navigate');
    component.back();

    expect(router.navigate).toHaveBeenCalledWith(['/customer']);
  });
});
