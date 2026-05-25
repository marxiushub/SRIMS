import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-staff',
  templateUrl: './staff.component.html',
  styleUrls: ['./staff.component.scss'],
  standalone: false
})
export class StaffComponent {

  constructor(private router: Router) { }

  //Checks whether a child route of StaffComponent is currently active
  isChildRouteActive(): boolean {
    return this.router.url !== '/staff' && this.router.url !== '/staff/';
  }
}
