import {Component} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {TranslateService} from '@ngx-translate/core';
import {StaffService} from '../../../services/staff.service';
import {StaffCreationDto} from '../../../dtos/staff-creation';

@Component({
  selector: 'app-register-staff',
  templateUrl: './register-staff.component.html',
  styleUrl: './register-staff.component.scss',
  standalone: false
})
export class RegisterStaffComponent {

  registerForm: UntypedFormGroup;
  submitted = false;
  showPassword = false;

  constructor(
    private formBuilder: UntypedFormBuilder,
    private staffService: StaffService,
    private router: Router,
    public translateService: TranslateService,
    private notification: ToastrService
  ) {
    this.registerForm = this.formBuilder.group({
      userName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required,
        Validators.minLength(10),
        Validators.pattern('^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$')
      ]]
    });
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.registerForm.invalid) {
      return;
    }

    const staffCreationDto: StaffCreationDto = {
      type: 'STAFF',
      userName: this.registerForm.controls.userName.value,
      password: this.registerForm.controls.password.value,
      email: this.registerForm.controls.email.value
    };

    this.staffService.createStaff(staffCreationDto).subscribe({
      next: () => {
        this.notification.success(this.translateService.instant('STAFF.REGISTER_STAFF.SUCCESS'));
        this.registerForm.reset();
        this.submitted = false;
        this.router.navigate(['/staff']);
      },
      error: error => this.handleError(error)
    });
  }

  back(): void {
    this.router.navigate(['/staff']);
  }

  private handleError(error: any): void {
    console.log('Action failed due to:', error);
    let errorMessage: string;

    if (error.error && typeof error.error === 'object') {
      if (error.error.errors) {
        errorMessage = error.error.errors;
      } else if (error.error.message) {
        errorMessage = error.error.message;
      } else if (error.error.error) {
        errorMessage = error.error.error;
      } else {
        errorMessage = this.translateService.instant('COMMON.UNKNOWN_ERROR') || 'An unexpected error occurred.';
      }
    } else if (error.error && typeof error.error === 'string') {
      errorMessage = error.error;
    } else {
      errorMessage = this.translateService.instant('COMMON.UNKNOWN_ERROR') || 'An unexpected error occurred.';
    }

    this.notification.error(errorMessage);
  }
}
