import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const homeGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return true;
  }

  const role = authService.getUserRole();
  console.log('HomeGuard -> User logged in. Role found:', role);

  if (role === 'ADMIN') {
    return router.navigate(['/staff']);
  } else if (role === 'USER') {
    return router.navigate(['/customer']);
  }

  console.warn('HomeGuard -> Logged in, but role matches neither "ADMIN" nor "USER".');
  return true;
};
