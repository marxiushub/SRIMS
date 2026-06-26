import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { StatisticsService } from '../../../services/statistics.service';
import { StatisticsRequestDto } from '../../../dtos/statistics-request';
import { StatisticsResponseDto } from '../../../dtos/statistics-response';
import { EquipmentType } from '../../../dtos/equipmenttype';
import {Equipment} from "../../../dtos/equipment";
import {Router} from "@angular/router";
import { StatisticsStateService } from '../../../services/statistics-state.service';
import { ErrorMappingService } from '../../../services/error-mapping.service';

interface RenderedRow {
  label: string;
  daysRented: number;
  id?: string;
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
  errorMessage: string | null = null;
  detailDegreeLabelKey = 'STAFF.STATISTICS.TABLE.MODEL_NAME';
  yAxisTicks: number[] = [];
  tableRows: RenderedRow[] = [];
  emptyReturnList = false;
  constructor(
    private fb: FormBuilder,
    private statisticsService: StatisticsService,
    private router: Router,
    private stateService: StatisticsStateService,
    private errorMapping: ErrorMappingService
  ) {}

  ngOnInit(): void {
    if (this.stateService.lastFilterValues) {
      this.filterForm = this.fb.group(this.stateService.lastFilterValues);
      this.tableRows = this.stateService.lastTableRows;
      this.maxDaysRented = this.stateService.lastMaxDaysRented;
      this.sortDescending = this.stateService.lastSortDescending;
      this.calculateYAxis();
    } else {
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
  }
  openDetailPage(row: RenderedRow): void {
    this.stateService.lastFilterValues = this.filterForm.value;
    this.stateService.lastTableRows = this.tableRows;
    this.stateService.lastMaxDaysRented = this.maxDaysRented;
    this.stateService.lastSortDescending = this.sortDescending;
    this.router.navigate(['/staff/inventory/view', row.id]);
  }

  onSubmit(): void {
    if (this.filterForm.invalid) return;
    this.emptyReturnList = false;
    this.isLoading = true;
    this.errorMessage = null;
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
            daysRented: days,
            id: id
          }));
        } else if (!response.detailDegree && response.modelCounts) {
          this.tableRows = Object.entries(response.modelCounts).map(([modelName, days]) => ({
            label: modelName,
            daysRented: days
          }));
        }
        if ((!response.itemCounts || Object.keys(response.itemCounts).length === 0) &&
          (!response.modelCounts || Object.keys(response.modelCounts).length === 0) ) {
          this.emptyReturnList = true;
        }
        this.tableRows.sort((a, b) => b.daysRented - a.daysRented);

        this.maxDaysRented = this.tableRows.length > 0
          ? Math.max(...this.tableRows.map(row => row.daysRented), 1)
          : 1;
        this.applySorting();
        this.calculateYAxis();
      },
      error: (error) => {
        console.error('Statistics Error:', error);
        this.isLoading = false;
        this.errorMessage = this.errorMapping.getErrorMessage(error);
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
