import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Equipment } from '../../../../dtos/equipment';
import { EquipmentService } from '../../../../services/equipment.service';
import { EquipmentType } from '../../../../dtos/equipmenttype';
import { TranslateService } from '@ngx-translate/core';
import { ErrorMappingService } from '../../../../services/error-mapping.service';

@Component({
  selector: 'app-customer-equipment-view',
  templateUrl: './equipment-view.component.html',
  styleUrl: './equipment-view.component.scss',
  standalone: false
})
export class CustomerEquipmentViewComponent implements OnInit {
  readonly EquipmentType = EquipmentType;

  equipment?: Equipment;
  loading = false;
  error = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private equipmentService: EquipmentService,
    public translateService: TranslateService,
    private errorMapping: ErrorMappingService
  ) {}

  // Initializes the component and loads equipment details based on the route parameter ID.
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadEquipment(Number(id));
    }
  }

  // Fetches equipment details from the backend and updates the component state.
  private loadEquipment(id: number): void {
    this.loading = true;
    this.error = false;
    this.equipmentService.getById(id).subscribe({
      next: (data) => {
        this.equipment = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load equipment details', err);
        this.error = true;
        this.loading = false;
        this.errorMessage = this.errorMapping.getErrorMessage(err);
      }
    });
  }

  // Navigates back to the customer inventory overview page.
  goBack(): void {
    this.router.navigate(['/customer/inventory']);
  }

  // Returns the corresponding CSS class for the given equipment status.
  getStatusClass(status: string): string {
    switch (status) {
      case 'FREE': return 'bg-success';
      case 'RESERVED': return 'bg-warning text-dark';
      case 'RENTED': return 'bg-danger';
      case 'MAINTENANCE': return 'bg-secondary';
      default: return 'bg-light text-dark';
    }
  }
}
