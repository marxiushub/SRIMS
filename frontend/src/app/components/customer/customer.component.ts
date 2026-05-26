import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-customer',
  templateUrl: './customer.component.html',
  styleUrls: ['./customer.component.scss'],
  standalone: false
})
export class CustomerComponent {

  constructor(private router: Router) { }

  /**
   * Checks whether a child route of CustomerComponent is currently active.
   */
  isChildRouteActive(): boolean {
    return this.router.url !== '/customer' && this.router.url !== '/customer/';
  }
}
