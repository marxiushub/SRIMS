import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NavbarService {
  closeNavbar$ = new Subject<void>();

  close() {
    this.closeNavbar$.next();
  }
}
