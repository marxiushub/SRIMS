import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {Router} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {ToastrModule, ToastrService} from 'ngx-toastr';
import {of, throwError} from 'rxjs';
import {RegisterStaffComponent} from './register-staff.component';
import {StaffService} from '../../../services/staff.service';
import {StaffCreationDto} from '../../../dtos/staff-creation';

describe('RegisterStaffComponent', () => {
  let component: RegisterStaffComponent;
  let fixture: ComponentFixture<RegisterStaffComponent>;
  let staffService: StaffService;
  let router: Router;
  let notification: ToastrService;

  beforeEach(() => {
    spyOn(console, 'log').and.stub();
  });

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [RegisterStaffComponent],
      imports: [RouterTestingModule, ReactiveFormsModule, TranslateModule.forRoot(), ToastrModule.forRoot()],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegisterStaffComponent);
    component = fixture.componentInstance;
    staffService = TestBed.inject(StaffService);
    router = TestBed.inject(Router);
    notification = TestBed.inject(ToastrService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  function fillValidForm() {
    component.registerForm.controls.userName.setValue('newstaff');
    component.registerForm.controls.email.setValue('newstaff@example.com');
    component.registerForm.controls.password.setValue('ValidPass1!');
  }

  it('should require userName, a valid email, and a valid password', () => {
    component.registerForm.controls.userName.setValue('');
    component.registerForm.controls.email.setValue('not-an-email');
    component.registerForm.controls.password.setValue('short');
    expect(component.registerForm.valid).toBeFalse();

    fillValidForm();
    expect(component.registerForm.valid).toBeTrue();
  });

  it('should call staffService.createStaff with the correct DTO and navigate to /staff on success', () => {
    spyOn(staffService, 'createStaff').and.returnValue(of({}));
    spyOn(router, 'navigate');
    spyOn(notification, 'success');

    fillValidForm();
    component.onSubmit();
    fixture.detectChanges();

    const expectedDto: StaffCreationDto = {
      type: 'STAFF',
      userName: 'newstaff',
      password: 'ValidPass1!',
      email: 'newstaff@example.com'
    };
    expect(staffService.createStaff).toHaveBeenCalledWith(expectedDto);
    expect(notification.success).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/staff']);
  });

  it('should show an error notification when staff creation fails', () => {
    spyOn(staffService, 'createStaff').and.returnValue(throwError(() => ({error: {message: 'Username already taken'}})));
    spyOn(notification, 'error');

    fillValidForm();
    component.onSubmit();
    fixture.detectChanges();

    expect(notification.error).toHaveBeenCalled();
  });


  it('should navigate back to /staff when back() is called', () => {
    spyOn(router, 'navigate');
    component.back();

    expect(router.navigate).toHaveBeenCalledWith(['/staff']);
  });
});
