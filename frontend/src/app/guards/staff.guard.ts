import {Router} from '@angular/router';
import {AuthService} from '../services/auth.service';
import {Injectable} from "@angular/core";

@Injectable({providedIn: 'root'})
export class StaffGuard {
  constructor(private authService: AuthService, private router: Router) {
  }

  canActivate(): boolean {
    if (this.authService.isLoggedIn() && this.authService.isStaff()) {
      return true;
    } else {
      this.router.navigate(['/']);
      return false;
    }
  }
}
