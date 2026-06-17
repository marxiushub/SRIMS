import {Component, OnInit} from '@angular/core';
import {AbstractControl, UntypedFormBuilder, UntypedFormGroup, ValidationErrors, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {AuthRequest} from '../../dtos/auth-request';
import {CustomerCreationDto} from '../../dtos/customer-creation';

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
  // Error flag
  error = false;
  errorMessage = '';

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private router: Router, private route: ActivatedRoute) {
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
            .toISOString()
            .split('T')[0]
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
   * Send authentication data to the authService. If the authentication was successfully, the user will be forwarded to the message page
   *
   * @param authRequest authentication data from the user login form
   */
  authenticateUser(authRequest: AuthRequest) {
    console.log('Try to authenticate user: ' + authRequest.email);
    this.authService.loginUser(authRequest).subscribe({
      next: () => {
        console.log('Successfully logged in user: ' + authRequest.email);
        this.router.navigate(['/message']);
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
        this.router.navigate(['/message']);
      },
      error: error => this.handleError(error)
    });
  }

  /**
   * Reusable error handler for HTTP failures
   */
  private handleError(error: any) {
    console.log('Action failed due to:');
    console.log(error);
    this.error = true;
    this.errorMessage = (typeof error.error === 'object') ? error.error.error : error.error;
  }

  /**
   * Error flag will be deactivated, which clears the error message
   */
  vanishError() {
    this.error = false;
  }

  ngOnInit() {
    this.route.data.subscribe(data => {
      if (data && data['mode'] !== undefined) {
        this.mode = data['mode'];
      }

      const registrationControls = ['repeatPassword', 'email', 'firstName', 'lastName', 'dateOfBirth'];

      if (this.mode === LoginRegisterMode.register) {
        this.loginForm.setValidators([this.passwordMatchValidator.bind(this)]);
      } else {
        registrationControls.forEach(control => this.loginForm.get(control)?.clearValidators());
        this.loginForm.clearValidators();
      }

      this.loginForm.updateValueAndValidity();
    });
  }

}
