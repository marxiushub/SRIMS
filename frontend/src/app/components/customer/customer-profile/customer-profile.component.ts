import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {CustomerService} from '../../../services/customer.service';
import {CustomerProfile} from '../../../dtos/customer-profile';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-customer-profile',
  templateUrl: './customer-profile.component.html',
  styleUrl: './customer-profile.component.scss',
  standalone: false
})
export class CustomerProfileComponent {
  profiles: CustomerProfile[] = [];
  loading = false;

  constructor(private customerService: CustomerService, public translateService: TranslateService, private router: Router) {
  }

  ngOnInit(): void {
    this.loadEquipment();
  }

  loadEquipment(): void {
    this.loading = true;

    this.customerService.getAll().subscribe({
      next: (data) => {
        //this.buildModelOptions(data);
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load equipment', err);
        this.loading = false;
      }
    })
  }

  openCreatePage(): void {
    this.router.navigate(['/customer/profiles/create']);
  }
}

