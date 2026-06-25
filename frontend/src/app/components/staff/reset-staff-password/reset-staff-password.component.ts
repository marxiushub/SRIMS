import {Component} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {TranslateService} from '@ngx-translate/core';
import {StaffService} from '../../../services/staff.service';

@Component({
  selector: 'app-reset-staff-password',
  templateUrl: './reset-staff-password.component.html',
  styleUrl: './reset-staff-password.component.scss',
  standalone: false
})
export class ResetStaffPasswordComponent {

  resetForm: UntypedFormGroup;
  submitted = false;

  constructor(
    private formBuilder: UntypedFormBuilder,
    private staffService: StaffService,
    private router: Router,
    public translateService: TranslateService,
    private notification: ToastrService
  ) {
    this.resetForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.resetForm.invalid) {
      return;
    }

    const email = this.resetForm.controls.email.value;
    this.staffService.resetPasswordForStaff(email).subscribe({
      next: () => {
        this.notification.success(this.translateService.instant('STAFF.RESET_PASSWORD.SUCCESS'));
        this.resetForm.reset();
        this.submitted = false;
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
