import {Component, OnInit} from '@angular/core';
import {EquipmentService} from '../../../services/equipment.service';
import {EquipmentOverview} from '../../../dtos/equipment-overview';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss',
  standalone: false
})
export class OverviewComponent implements OnInit {

  overview: EquipmentOverview | null = null;
  loading = false;
  errorMessageKey: string | null = null;

  constructor(private equipmentService: EquipmentService) {
  }

  ngOnInit(): void {
    this.loadOverview();
  }

  loadOverview(): void {
    this.loading = true;
    this.errorMessageKey = null;

    this.equipmentService.getStatusOverview().subscribe({
      next: (data) => {
        this.overview = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load equipment status overview', err);
        this.errorMessageKey = 'STAFF.OVERVIEW.MESSAGES.ERROR';
        this.loading = false;
      }
    });
  }
}
