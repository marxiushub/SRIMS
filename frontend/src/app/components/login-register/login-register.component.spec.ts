import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {Router} from '@angular/router';
import {LoginRegisterComponent, LoginRegisterMode} from './login-register.component';
import {AuthService} from '../../services/auth.service';
import {ToastrService} from 'ngx-toastr';
import { TranslateModule } from '@ngx-translate/core';
import { ToastrModule } from 'ngx-toastr';
import { of, throwError } from 'rxjs';

describe('LoginRegisterComponent', () => {
  let component: LoginRegisterComponent;
  let fixture: ComponentFixture<LoginRegisterComponent>;
  let authService: AuthService;
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
    router = TestBed.inject(Router);
    notification = TestBed.inject(ToastrService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
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
