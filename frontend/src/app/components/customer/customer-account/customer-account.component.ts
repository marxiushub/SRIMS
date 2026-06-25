import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {TranslateService} from '@ngx-translate/core';
import {AuthService} from '../../../services/auth.service';
import {CustomerService} from '../../../services/customer.service';
import {CustomerSearchResponse} from '../../../dtos/customer-search-response';
import {PasswordChange} from '../../../dtos/password-change';

@Component({
  selector: 'app-customer-account',
  templateUrl: './customer-account.component.html',
  styleUrl: './customer-account.component.scss',
  standalone: false
})
export class CustomerAccountComponent implements OnInit {

  account?: CustomerSearchResponse;
  loading = false;
  loadError = false;

  passwordForm: UntypedFormGroup;
  passwordSubmitted = false;
  showOldPassword = false;
  showNewPassword = false;

  private customerId: number | null = null;

  constructor(
    private formBuilder: UntypedFormBuilder,
    private authService: AuthService,
    private customerService: CustomerService,
    private router: Router,
    public translateService: TranslateService,
    private notification: ToastrService
  ) {
    this.passwordForm = this.formBuilder.group({
      oldPassword: ['', [Validators.required]],
      newPassword: ['', [
        Validators.required,
        Validators.minLength(10),
        Validators.pattern('^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$')
      ]]
    });
  }

  ngOnInit(): void {
    this.customerId = this.authService.getUserId();
    if (this.customerId != null) {
      this.loadAccount(this.customerId);
    } else {
      this.loadError = true;
    }
  }

  loadAccount(id: number): void {
    this.loading = true;
    this.loadError = false;
    this.customerService.getById(id).subscribe({
      next: (data) => {
        this.account = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load account', err);
        this.loadError = true;
        this.loading = false;
      }
    });
  }

  onSubmitPasswordChange(): void {
    this.passwordSubmitted = true;
    if (this.passwordForm.invalid || this.customerId == null) {
      return;
    }

    const passwordChange: PasswordChange = {
      oldPassword: this.passwordForm.controls.oldPassword.value,
      newPassword: this.passwordForm.controls.newPassword.value
    };

    this.customerService.changePassword(this.customerId, passwordChange).subscribe({
      next: () => {
        this.notification.success(this.translateService.instant('CUSTOMER.ACCOUNT.PASSWORD_CHANGE_SUCCESS'));
        this.passwordForm.reset();
        this.passwordSubmitted = false;
      },
      error: error => this.handleError(error)
    });
  }

  back(): void {
    this.router.navigate(['/customer']);
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
