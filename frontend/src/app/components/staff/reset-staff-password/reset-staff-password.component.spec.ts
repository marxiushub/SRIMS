import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {Router} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {ToastrModule, ToastrService} from 'ngx-toastr';
import {of, throwError} from 'rxjs';
import {ResetStaffPasswordComponent} from './reset-staff-password.component';
import {StaffService} from '../../../services/staff.service';

describe('ResetStaffPasswordComponent', () => {
  let component: ResetStaffPasswordComponent;
  let fixture: ComponentFixture<ResetStaffPasswordComponent>;
  let staffService: StaffService;
  let router: Router;
  let notification: ToastrService;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ResetStaffPasswordComponent],
      imports: [RouterTestingModule, ReactiveFormsModule, TranslateModule.forRoot(), ToastrModule.forRoot()],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResetStaffPasswordComponent);
    component = fixture.componentInstance;
    staffService = TestBed.inject(StaffService);
    router = TestBed.inject(Router);
    notification = TestBed.inject(ToastrService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should require a valid email', () => {
    component.resetForm.controls.email.setValue('');
    expect(component.resetForm.valid).toBeFalse();

    component.resetForm.controls.email.setValue('not-an-email');
    expect(component.resetForm.valid).toBeFalse();

    component.resetForm.controls.email.setValue('staffmember@example.com');
    expect(component.resetForm.valid).toBeTrue();
  });

  it('should call staffService.resetPasswordForStaff and reset the form on success', () => {
    spyOn(staffService, 'resetPasswordForStaff').and.returnValue(of({}));
    spyOn(notification, 'success');

    component.resetForm.controls.email.setValue('staffmember@example.com');
    component.onSubmit();

    expect(staffService.resetPasswordForStaff).toHaveBeenCalledWith('staffmember@example.com');
    expect(notification.success).toHaveBeenCalled();
    expect(component.resetForm.controls.email.value).toBe('');
  });

  it('should show an error notification when the reset request fails', () => {
    spyOn(staffService, 'resetPasswordForStaff').and.returnValue(throwError(() => ({error: {message: 'Email not found'}})));
    spyOn(notification, 'error');

    component.resetForm.controls.email.setValue('unknown@example.com');
    component.onSubmit();

    expect(notification.error).toHaveBeenCalled();
  });

  it('should not call resetPasswordForStaff when the form is invalid', () => {
    spyOn(staffService, 'resetPasswordForStaff');

    component.resetForm.controls.email.setValue('not-an-email');
    component.onSubmit();

    expect(staffService.resetPasswordForStaff).not.toHaveBeenCalled();
  });

  it('should navigate back to /staff when back() is called', () => {
    spyOn(router, 'navigate');
    component.back();

    expect(router.navigate).toHaveBeenCalledWith(['/staff']);
  });
});
