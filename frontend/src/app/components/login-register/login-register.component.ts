import {Component, OnInit} from '@angular/core';
import {AbstractControl, UntypedFormBuilder, UntypedFormGroup, ValidationErrors, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {AuthRequest} from '../../dtos/auth-request';
import {CustomerCreationDto} from '../../dtos/customer-creation';
import {ToastrService} from "ngx-toastr";
import {TranslateService} from "@ngx-translate/core";

export enum LoginRegisterMode {
  login,
  register
}

@Component({
  selector: 'app-login',
  templateUrl: './login-register.component.html',
  styleUrls: ['./login-register.component.scss'],
  standalone: false
})
export class LoginRegisterComponent implements OnInit {
  readonly LoginRegisterMode = LoginRegisterMode;
  mode: LoginRegisterMode = LoginRegisterMode.login;

  loginForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;
  errorMessage = '';

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, public translateService: TranslateService, private router: Router, private route: ActivatedRoute, private notification: ToastrService) {
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      repeatPassword: ['', [Validators.required]],
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      dateOfBirth: ['', [Validators.required]]
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
          userName: this.loginForm.controls.username.value,
          password: this.loginForm.controls.password.value,
          email: this.loginForm.controls.email.value,
          firstName: this.loginForm.controls.firstName.value,
          lastName: this.loginForm.controls.lastName.value,
          dateOfBirth: this.loginForm.controls.dateOfBirth.value
        };
        console.log(customerDto)
        this.registerUser(customerDto);
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
        this.notification.success(this.translateService.instant('COMMON.LOGIN_SUCCESS'));
        this.router.navigate(['/']);
      },
      error: error => this.handleError(error)
    });
  }

  /**
   * Send registration data to the authService to create a new account
   */
  registerUser(customerDto: CustomerCreationDto) {
    console.log('Registering customer: ' + customerDto.userName);
    this.authService.registerUser(customerDto).subscribe({
      next: () => {
        console.log('Successfully registered user: ' + customerDto.email);
        this.notification.success(this.translateService.instant('COMMON.REGISTER_SUCCESS'));
        this.router.navigate(['/']);
      },
      error: error => this.handleError(error)
    });
  }

  /**
   * Reusable error handler for HTTP failures
   */
  private handleError(error: any) {
    console.log('Action failed due to:');
    this.errorMessage = (typeof error.error === 'object') ? error.error.error : error.error;
    this.notification.error(this.errorMessage);
  }

  ngOnInit() {
    this.route.data.subscribe(data => {
      if (data && data['mode'] !== undefined) {
        this.mode = data['mode'];
      }

      const registrationControls = ['repeatPassword', 'email', 'firstName', 'lastName', 'dateOfBirth'];
      const usernameControl = this.loginForm.get('username');

      if (this.mode === LoginRegisterMode.register) {
        usernameControl?.setValidators([Validators.required]);
        this.loginForm.setValidators([this.passwordMatchValidator.bind(this)]);
      } else {
        usernameControl?.setValidators([Validators.required, Validators.email]);
        registrationControls.forEach(control => this.loginForm.get(control)?.clearValidators());
        this.loginForm.clearValidators();
      }

      usernameControl?.updateValueAndValidity();
      registrationControls.forEach(control => this.loginForm.get(control)?.updateValueAndValidity());
      this.loginForm.updateValueAndValidity();
    });
  }

}
