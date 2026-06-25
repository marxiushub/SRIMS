import {Component, OnInit} from '@angular/core';
import {
  AbstractControl,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import { CustomerProfileService } from '../../services/customer-profile.service';
import {AuthRequest} from '../../dtos/auth-request';
import {CustomerCreationDto} from '../../dtos/customer-creation';
import { CustomerProfileCreationUpdate } from '../../dtos/customer-profile-creation-update';
import { SkillLevel } from '../../dtos/skilllevel';
import {ToastrService} from "ngx-toastr";
import {TranslateService} from "@ngx-translate/core";
import { switchMap } from 'rxjs/operators';

export enum LoginRegisterMode {
  login,
  register,
  resetPassword
}

export function maxDateTodayValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }

    const inputDate = new Date(control.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return inputDate > today ? {futureDate: true} : null;
  };
}

@Component({
  selector: 'app-login',
  templateUrl: './login-register.component.html',
  styleUrls: ['./login-register.component.scss'],
  standalone: false
})
export class LoginRegisterComponent implements OnInit {
  readonly LoginRegisterMode = LoginRegisterMode;
  readonly SkillLevelEnum = SkillLevel;

  mode: LoginRegisterMode = LoginRegisterMode.login;

  loginForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;
  errorMessage = '';
  returnUrl: string = '';
  showPassword = false;
  showRepeatPassword = false;

  skillLevels = [
    SkillLevel.BEGINNER,
    SkillLevel.INTERMEDIATE,
    SkillLevel.ADVANCED
  ];

  constructor(
      private formBuilder: UntypedFormBuilder,
      private authService: AuthService,
      private customerProfileService: CustomerProfileService,
      public translateService: TranslateService,
      private router: Router,
      private route: ActivatedRoute,
      private notification: ToastrService) {
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]],
      repeatPassword: ['', [Validators.required]],
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      dateOfBirth: ['', [Validators.required, maxDateTodayValidator()]],
      height: [null],
      weight: [null],
      shoeSize: [null],
      skillLevel: [null]
    });
  }

  /**
   * Custom validator to verify both password fields hold matching text
   */
  passwordMatchValidator(formGroup: AbstractControl): ValidationErrors | null {
    const password = formGroup.get('password')?.value;
    const repeatPassword = formGroup.get('repeatPassword')?.value;
    return password === repeatPassword ? null : {passwordsMismatch: true};
  }

  /**
   * Form validation will start after the method is called, additionally an AuthRequest will be sent
   */
  onSubmit() {
    this.submitted = true;
    if (this.loginForm.valid) {
      if (this.mode === LoginRegisterMode.register) {
        const customerDto: CustomerCreationDto = {
          type: 'CUSTOMER',
          userName: this.loginForm.controls.username.value,
          password: this.loginForm.controls.password.value,
          email: this.loginForm.controls.email.value,
          firstName: this.loginForm.controls.firstName.value,
          lastName: this.loginForm.controls.lastName.value,
          dateOfBirth: this.loginForm.controls.dateOfBirth.value,
        };
        const profileDto: CustomerProfileCreationUpdate = {
          profileName: this.loginForm.controls.username.value,
          height: Number(this.loginForm.controls.height.value),
          weight: Number(this.loginForm.controls.weight.value),
          shoeSize: Number(this.loginForm.controls.shoeSize.value),
          skillLevel: this.loginForm.controls.skillLevel.value
        };
        console.log(customerDto)
        this.registerUser(customerDto);
      } else if (this.mode === LoginRegisterMode.resetPassword) {
        this.requestPasswordReset(this.loginForm.controls.username.value);
      } else {
        const authRequest: AuthRequest = new AuthRequest(this.loginForm.controls.username.value, this.loginForm.controls.password.value);
        this.authenticateUser(authRequest);
      }
    } else {
      console.log('Invalid input');
    }
  }

  /**
   * Send authentication data to the authService. If the authentication was successfully, the user will be forwarded to the home page
   *
   * @param authRequest authentication data from the user login form
   */
  authenticateUser(authRequest: AuthRequest) {
    console.log('Try to authenticate user: ' + authRequest.email);
    this.authService.loginUser(authRequest).subscribe({
      next: () => {
        console.log('Successfully logged in user: ' + authRequest.email);

        const role = this.authService.getUserRole();
        if (role === 'USER' && this.returnUrl) {
          this.router.navigateByUrl(this.returnUrl);
        } else if (role === 'ADMIN') {
          this.router.navigate(['/staff']);
        } else {
          this.router.navigate(['/customer']);
        }
      },
      error: error => this.handleError(error)
    });
  }

  /**
   * Send registration data to the authService to create a new account
   */
  registerUserWithProfile(customerDto: CustomerCreationDto, profileDto: CustomerProfileCreationUpdate) {
    console.log('Registering customer: ' + customerDto.userName);

    this.authService.registerUser(customerDto).pipe(
        switchMap(() => {
          const loginCredentials = new AuthRequest(customerDto.email, customerDto.password);
          return this.authService.loginUser(loginCredentials);
        }),
        switchMap(() => {
          return this.customerProfileService.create(profileDto);
        })
    ).subscribe({
      next: () => {
        console.log('Successfully registered user and created primary profile.');
        this.notification.success(this.translateService.instant('COMMON.REGISTER_SUCCESS'));
        this.router.navigate(['/customer']);
      },
      error: error => this.handleError(error)
    });
  }

  /**
   * Send a password reset request for the given email to the authService.
   * On success, the backend sends a new generated password to that email address.
   *
   * @param email the email of the customer who forgot their password
   */
  requestPasswordReset(email: string) {
    console.log('Requesting password reset for: ' + email);
    this.authService.resetPassword(email).subscribe({
      next: () => {
        console.log('Successfully requested password reset for: ' + email);
        this.notification.success(this.translateService.instant('COMMON.PASSWORD_RESET_SUCCESS'));
        this.router.navigate(['/login']);
      },
      error: error => this.handleError(error)
    });
  }

  /**
   * Reusable error handler for HTTP failures
   */
  private handleError(error: any) {
    console.log('Action failed due to:', error);

    if (error.error && typeof error.error === 'object') {
      if (error.error.errors) {
        this.errorMessage = error.error.errors;
      } else if (error.error.message) {
        this.errorMessage = error.error.message;
      } else if (error.error.error) {
        this.errorMessage = error.error.error;
      }
    } else if (error.error && typeof error.error === 'string') {
      this.errorMessage = error.error;
    } else {
      this.errorMessage = this.translateService.instant('COMMON.UNKNOWN_ERROR') || 'An unexpected error occurred.';
    }

    this.notification.error(this.errorMessage);
  }

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  logout() {
    this.authService.logoutUser();
    console.log('User logged out successfully.');
  }

  ngOnInit() {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '';
    this.route.data.subscribe(data => {
      if (data && data['mode'] !== undefined) {
        this.mode = data['mode'];
      }

      const registrationControls = ['repeatPassword', 'email', 'firstName', 'lastName', 'dateOfBirth'];
      const usernameControl = this.loginForm.get('username');
      const passwordControl = this.loginForm.get('password');

      if (this.mode === LoginRegisterMode.register) {
        usernameControl?.setValidators([Validators.required]);
        passwordControl?.setValidators([
          Validators.required,
          Validators.minLength(10),
          Validators.pattern('^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$')
        ]);
        this.loginForm.get('height')?.setValidators([Validators.required, Validators.min(50), Validators.max(250)]);
        this.loginForm.get('weight')?.setValidators([Validators.required, Validators.min(10), Validators.max(250)]);
        this.loginForm.get('shoeSize')?.setValidators([Validators.required, Validators.min(15), Validators.max(60)]);
        this.loginForm.get('skillLevel')?.setValidators([Validators.required]);

        this.loginForm.setValidators([this.passwordMatchValidator.bind(this)]);
      } else if (this.mode === LoginRegisterMode.resetPassword) {
        usernameControl?.setValidators([Validators.required, Validators.email]);
        passwordControl?.clearValidators();
        registrationControls.forEach(control => this.loginForm.get(control)?.clearValidators());
        this.loginForm.clearValidators();
      } else {
        usernameControl?.setValidators([Validators.required, Validators.email]);
        passwordControl?.setValidators([Validators.required]);
        registrationControls.forEach(control => this.loginForm.get(control)?.clearValidators());
        this.loginForm.clearValidators();
      }

      usernameControl?.updateValueAndValidity();
      passwordControl?.updateValueAndValidity();
      registrationControls.forEach(control => this.loginForm.get(control)?.updateValueAndValidity());
      this.loginForm.updateValueAndValidity();
    });
  }
}
