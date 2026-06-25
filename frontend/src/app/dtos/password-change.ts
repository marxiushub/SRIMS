/**
 * DTO mirroring the backend's PasswordChangeDto.
 * Used to submit an old/new password pair when changing the password
 * of an already logged-in customer.
 */
export interface PasswordChange {
  oldPassword: string;
  newPassword: string;
}
