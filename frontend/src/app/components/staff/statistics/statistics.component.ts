import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { StatisticsService } from '../../../services/statistics.service';


import { StatisticsRequestDto } from '../../../dtos/statistics-request';
import { StatisticsResponseDto } from '../../../dtos/statistics-response';
import { EquipmentType } from '../../../dtos/equipmenttype';

interface RenderedRow {
  label: string;
  daysRented: number;
}

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss'],
  standalone: false
})
export class StatisticsComponent implements OnInit {
  filterForm!: FormGroup;
  isLoading = false;
  maxDaysRented = 1;
  sortDescending = true;
  errorMessageKey: string | null = null;
  detailDegreeLabelKey = 'STAFF.STATISTICS.TABLE.MODEL_NAME';
  yAxisTicks: number[] = [];
  tableRows: RenderedRow[] = [];

  constructor(
    private fb: FormBuilder,
    private statisticsService: StatisticsService
  ) {}

  ngOnInit(): void {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(endDate.getDate() - 30);

    this.filterForm = this.fb.group({
      searchStart: [startDate.toISOString().split('T')[0], Validators.required],
      searchEnd: [endDate.toISOString().split('T')[0], Validators.required],
      type: [''],
      detailDegree: [false]
    });
  }

  onSubmit(): void {
    if (this.filterForm.invalid) return;

    this.isLoading = true;
    this.errorMessageKey = null;
    this.tableRows = [];

    const requestData: StatisticsRequestDto = { ...this.filterForm.value };

    if (requestData.type === '' as any) {
      requestData.type = null as unknown as EquipmentType;
    }

    this.statisticsService.getEquipmentStatistics(requestData).subscribe({
      next: (response: StatisticsResponseDto) => {
        this.isLoading = false;

        if (response.detailDegree && response.itemCounts) {
          this.tableRows = Object.entries(response.itemCounts).map(([id, days]) => ({
            label: `#${id}`,
            daysRented: days
          }));
        } else if (!response.detailDegree && response.modelCounts) {
          this.tableRows = Object.entries(response.modelCounts).map(([modelName, days]) => ({
            label: modelName,
            daysRented: days
          }));
        }

        this.tableRows.sort((a, b) => b.daysRented - a.daysRented);

        this.maxDaysRented = this.tableRows.length > 0
          ? Math.max(...this.tableRows.map(row => row.daysRented), 1)
          : 1;
        this.applySorting();
        this.calculateYAxis();
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessageKey = 'STAFF.STATISTICS.MESSAGES.ERROR';
        console.error('Statistics Error:', error);
      }
    });
  }
  toggleSort(): void {
    this.sortDescending = !this.sortDescending;
    this.applySorting();
  }
  private applySorting(): void {
    if (this.sortDescending) {
      this.tableRows.sort((a, b) => b.daysRented - a.daysRented);
    } else {
      this.tableRows.sort((a, b) => a.daysRented - b.daysRented);
    }
  }
  private calculateYAxis(): void {
    this.yAxisTicks = [];
    const maxDataVal = this.tableRows.length > 0
      ? Math.max(...this.tableRows.map(row => row.daysRented), 1)
      : 1;

    if (maxDataVal <= 10) {

      this.maxDaysRented = maxDataVal;
      for (let i = 0; i <= this.maxDaysRented; i++) {
        this.yAxisTicks.push(i);
      }
    } else {

      const roughStep = maxDataVal / 5;
      const magnitude = Math.pow(10, Math.floor(Math.log10(roughStep)));
      const step = Math.max(Math.ceil(roughStep / magnitude) * magnitude, 1);


      this.maxDaysRented = Math.ceil(maxDataVal / step) * step;


      for (let i = 0; i <= this.maxDaysRented; i += step) {
        this.yAxisTicks.push(i);
      }
    }
  }
}
