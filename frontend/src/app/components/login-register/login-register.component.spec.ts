import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {Router} from '@angular/router';
import {LoginRegisterComponent, LoginRegisterMode} from './login-register.component';
import {AuthService} from '../../services/auth.service';
import {CustomerProfileService} from '../../services/customer-profile.service';
import {ToastrService} from 'ngx-toastr';
import { TranslateModule } from '@ngx-translate/core';
import { ToastrModule } from 'ngx-toastr';
import { of, throwError } from 'rxjs';
import {CustomerCreationDto} from '../../dtos/customer-creation';
import {SkillLevel} from '../../dtos/skilllevel';

describe('LoginRegisterComponent', () => {
  let component: LoginRegisterComponent;
  let fixture: ComponentFixture<LoginRegisterComponent>;
  let authService: AuthService;
  let customerProfileService: CustomerProfileService;
  let router: Router;
  let notification: ToastrService;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [LoginRegisterComponent],
      imports: [RouterTestingModule, ReactiveFormsModule, TranslateModule.forRoot(), ToastrModule.forRoot()],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginRegisterComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    customerProfileService = TestBed.inject(CustomerProfileService);
    router = TestBed.inject(Router);
    notification = TestBed.inject(ToastrService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('register mode', () => {
    beforeEach(() => {
      component.mode = LoginRegisterMode.register;
      component.ngOnInit();
    });

    function fillValidRegistrationForm() {
      component.loginForm.controls.username.setValue('newuser');
      component.loginForm.controls.password.setValue('ValidPass1!');
      component.loginForm.controls.repeatPassword.setValue('ValidPass1!');
      component.loginForm.controls.firstName.setValue('Jane');
      component.loginForm.controls.lastName.setValue('Doe');
      component.loginForm.controls.email.setValue('jane.doe@example.com');
      component.loginForm.controls.dateOfBirth.setValue('1995-05-05');
      component.loginForm.controls.height.setValue(170);
      component.loginForm.controls.weight.setValue(65);
      component.loginForm.controls.shoeSize.setValue(42);
      component.loginForm.controls.skillLevel.setValue(SkillLevel.BEGINNER);
    }

    it('should chain registerUser, loginUser, and customerProfileService.create on submit', () => {
      spyOn(authService, 'registerUser').and.returnValue(of({}));
      spyOn(authService, 'loginUser').and.returnValue(of('fake-token'));
      spyOn(customerProfileService, 'create').and.returnValue(of({} as any));
      spyOn(router, 'navigate');
      spyOn(notification, 'success');

      fillValidRegistrationForm();
      component.onSubmit();

      expect(authService.registerUser).toHaveBeenCalled();
      const registeredDto: CustomerCreationDto = (authService.registerUser as jasmine.Spy).calls.mostRecent().args[0];
      expect(registeredDto.userName).toBe('newuser');
      expect(registeredDto.email).toBe('jane.doe@example.com');

      expect(authService.loginUser).toHaveBeenCalled();
      expect(customerProfileService.create).toHaveBeenCalled();
      const createdProfile = (customerProfileService.create as jasmine.Spy).calls.mostRecent().args[0];
      expect(createdProfile.profileName).toBe('newuser');
      expect(createdProfile.height).toBe(170);
      expect(createdProfile.skillLevel).toBe(SkillLevel.BEGINNER);

      expect(notification.success).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/customer']);
    });

    it('should show an error notification when registration fails', () => {
      spyOn(authService, 'registerUser').and.returnValue(throwError(() => ({error: {message: 'Email already in use'}})));
      spyOn(authService, 'loginUser');
      spyOn(customerProfileService, 'create');
      spyOn(notification, 'error');

      fillValidRegistrationForm();
      component.onSubmit();

      expect(notification.error).toHaveBeenCalled();
      expect(authService.loginUser).not.toHaveBeenCalled();
      expect(customerProfileService.create).not.toHaveBeenCalled();
    });

    it('should show an error notification when profile creation fails after successful registration and login', () => {
      spyOn(authService, 'registerUser').and.returnValue(of({}));
      spyOn(authService, 'loginUser').and.returnValue(of('fake-token'));
      spyOn(customerProfileService, 'create').and.returnValue(throwError(() => ({error: {message: 'Invalid profile data'}})));
      spyOn(notification, 'error');

      fillValidRegistrationForm();
      component.onSubmit();

      expect(notification.error).toHaveBeenCalled();
    });

    it('should not submit when the registration form is invalid', () => {
      spyOn(authService, 'registerUser');

      component.loginForm.controls.username.setValue('');
      component.onSubmit();

      expect(authService.registerUser).not.toHaveBeenCalled();
    });
  });

  describe('resetPassword mode', () => {
    beforeEach(() => {
      component.mode = LoginRegisterMode.resetPassword;
      component.ngOnInit();
    });

    it('should require only the username (email) control', () => {
      component.loginForm.controls.username.setValue('');
      expect(component.loginForm.controls.username.valid).toBeFalse();

      component.loginForm.controls.username.setValue('not-an-email');
      expect(component.loginForm.controls.username.valid).toBeFalse();

      component.loginForm.controls.username.setValue('customer@example.com');
      expect(component.loginForm.controls.username.valid).toBeTrue();
    });

    it('should not require password or registration fields', () => {
      component.loginForm.controls.username.setValue('customer@example.com');
      expect(component.loginForm.valid).toBeTrue();
    });

    it('should call authService.resetPassword and navigate to /login on success', () => {
      spyOn(authService, 'resetPassword').and.returnValue(of({}));
      spyOn(router, 'navigate');
      spyOn(notification, 'success');

      component.loginForm.controls.username.setValue('customer@example.com');
      component.onSubmit();

      expect(authService.resetPassword).toHaveBeenCalledWith('customer@example.com');
      expect(notification.success).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should show an error notification when the reset request fails', () => {
      spyOn(authService, 'resetPassword').and.returnValue(throwError(() => ({error: {message: 'Email not found'}})));
      spyOn(notification, 'error');

      component.loginForm.controls.username.setValue('unknown@example.com');
      component.onSubmit();

      expect(notification.error).toHaveBeenCalled();
    });

    it('should not call resetPassword when the form is invalid', () => {
      spyOn(authService, 'resetPassword');

      component.loginForm.controls.username.setValue('not-an-email');
      component.onSubmit();

      expect(authService.resetPassword).not.toHaveBeenCalled();
    });
  });
});
