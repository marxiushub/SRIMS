import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ReservationService} from '../../../../services/reservation.service';
import {EquipmentService} from '../../../../services/equipment.service';
import {TranslateService} from '@ngx-translate/core';
import {ToastrService} from 'ngx-toastr';
import {Equipment} from '../../../../dtos/equipment';
import {EquipmentSearch} from '../../../../dtos/equipment-search';
import {EquipmentType} from '../../../../dtos/equipmenttype';
import {SkillLevel} from '../../../../dtos/skilllevel';
import {ReservationUpdate} from '../../../../dtos/reservation-update';
import {debounceTime, distinctUntilChanged, forkJoin} from 'rxjs';
import {ErrorMappingService} from '../../../../services/error-mapping.service';

@Component({
  selector: 'app-staff-reservation-edit',
  templateUrl: './staff-reservation-edit.component.html',
  styleUrls: ['./staff-reservation-edit.component.scss'],
  standalone: false
})
export class StaffReservationEditComponent implements OnInit {
  readonly EquipmentTypeEnum = EquipmentType;

  reservationId!: number;
  reservationForm!: FormGroup;
  selectedEquipment: Equipment[] = [];
  availableEquipmentList: Equipment[] = [];

  customerName = '';
  customerProfileId!: number;

  currentActiveType: EquipmentType | null = null;
  modelFilter = '';
  skillFilter: SkillLevel | null = null;
  priceSortDirection: 'asc' | 'desc' | '' = 'asc';

  filtersExpanded: boolean = false;
  equipmentListExpanded: boolean = true;

  loading = false;
  submitLoading = false;
  submitError: string | null = null;
  validationWarning?: string;

  private originalStartDate!: string;
  private originalEndDate!: string;
  private selectedEquipmentBackup: Equipment[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private reservationService: ReservationService,
    private equipmentService: EquipmentService,
    private notification: ToastrService,
    public translateService: TranslateService,
    private errorMapping: ErrorMappingService
  ) {
  }

  ngOnInit(): void {
    this.initForm();
    this.initDateChangeWatcher();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.reservationId = Number(id);
      this.loadReservation();
    }
  }

  /**
   * Initializes the form with validators conforming to the ones used in the backend.
   */
  private initForm(): void {
    this.reservationForm = this.fb.group({
      startDate: [null, Validators.required],
      pickUpTime: ['09:00', Validators.required],
      endDate: [null, Validators.required]
    });

    this.reservationForm.valueChanges.subscribe(values => {
      if (this.currentActiveType && values.startDate && values.endDate && !this.isDateRangeInvalid) {
        this.searchEquipment();
      }
    });
  }

  /**
   * Watches for changes in startDate or endDate to re-validate already selected equipment.
   */
  private initDateChangeWatcher(): void {
    this.reservationForm.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged((prev, curr) =>
        prev.startDate === curr.startDate && prev.endDate === curr.endDate
      )
    ).subscribe(values => {
      if (this.selectedEquipmentBackup.length > 0 && values.startDate && values.endDate && !this.isDateRangeInvalid) {
        this.validateSelectedEquipmentForNewDates(values.startDate, values.endDate);
      }
    });
  }

  /**
   * Validates already selected equipment against a newly chosen date range.
   * Removes equipment that is no longer available.
   */
  public validateSelectedEquipmentForNewDates(startDate: string, endDate: string): void {
    const originalStart = new Date(this.originalStartDate);
    const originalEnd = new Date(this.originalEndDate);
    const newStart = new Date(startDate);
    const newEnd = new Date(endDate);

    // Case 1: New Date Range is entirely within original one (or identical) - restore with Backup
    if (newStart >= originalStart && newEnd <= originalEnd) {
      this.selectedEquipment = [...this.selectedEquipmentBackup];
      this.validationWarning = undefined;
      return;
    }

    const requests = [];

    // Case 2: Complete change of dates so that there is no more overlap with the original
    if (newEnd < originalStart || newStart > originalEnd) {
      requests.push(this.equipmentService.search({
        start: startDate,
        end: endDate
      }));
    }
    // Case 3: Partial Overlap with original date range - Calculation of the new days to check
    else {
      if (newStart < originalStart) {
        const dayBeforeOriginalStart = new Date(originalStart);
        dayBeforeOriginalStart.setDate(dayBeforeOriginalStart.getDate() - 1);

        const formattedDate = dayBeforeOriginalStart.getFullYear() + '-' +
          String(dayBeforeOriginalStart.getMonth() + 1).padStart(2, '0') + '-' +
          String(dayBeforeOriginalStart.getDate()).padStart(2, '0');

        requests.push(this.equipmentService.search({
          start: startDate,
          end: formattedDate
        }));
      }
      if (newEnd > originalEnd) {
        const dayAfterOriginalEnd = new Date(originalEnd);
        dayAfterOriginalEnd.setDate(dayAfterOriginalEnd.getDate() + 1);

        const formattedDate = dayAfterOriginalEnd.getFullYear() + '-' +
          String(dayAfterOriginalEnd.getMonth() + 1).padStart(2, '0') + '-' +
          String(dayAfterOriginalEnd.getDate()).padStart(2, '0');

        requests.push(this.equipmentService.search({
          start: formattedDate,
          end: endDate
        }));
      }
    }

    forkJoin(requests).subscribe({
      next: (results) => {
        let availableIds: number[] = [];

        if (results.length === 1) {
          availableIds = results[0].map((e: any) => e.id);
        } else if (results.length === 2) {
          const idsFront = results[0].map((e: any) => e.id);
          const idsBack = results[1].map((e: any) => e.id);
          availableIds = idsFront.filter((id: number) => idsBack.includes(id));
        }

        const filteredList = this.selectedEquipmentBackup.filter(item => availableIds.includes(item.id));

        if (filteredList.length !== this.selectedEquipmentBackup.length) {
          this.selectedEquipment = filteredList;
          this.validationWarning = 'RESERVATION.VALIDATION_WARNING';
          setTimeout(() => this.validationWarning = undefined, 8000);
        } else {
          this.selectedEquipment = filteredList;
          this.validationWarning = undefined;
        }
      },
      error: (err) => {
        console.error('Failed to validate selected equipment on date change', err);
      }
    });
  }

  /**
   * Checks if the date configuration is invalid.
   */
  get isDateRangeInvalid(): boolean {
    const start = this.reservationForm.get('startDate')?.value;
    const end = this.reservationForm.get('endDate')?.value;
    if (!start || !end) {
      return false;
    }
    return new Date(end) < new Date(start);
  }

  /**
   * Checks if the start date is in the past.
   */
  get isStartDateInPast(): boolean {
    const start = this.reservationForm.get('startDate')?.value;
    if (!start) return false;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return new Date(start) < today;
  }

  /**
   * Checks if the end date is in the past.
   */
  get isStartDateMissing(): boolean {
    return !this.reservationForm.get('startDate')?.value;
  }

  /**
   * Checks if the end date is missing.
   */
  get isEndDateMissing(): boolean {
    return !this.reservationForm.get('endDate')?.value;
  }

  /**
   * Calculates the current live total price based on selected equipment and days.
   */
  get currentTotalPrice(): number {
    const start = this.reservationForm.get('startDate')?.value;
    const end = this.reservationForm.get('endDate')?.value;

    if (!start || !end || this.isDateRangeInvalid || this.selectedEquipment.length === 0) {
      return 0;
    }

    const startDate = new Date(start);
    const endDate = new Date(end);

    //Calculate difference in milliseconds, then transform back to dates to get the difference in days
    const diffTime = endDate.getTime() - startDate.getTime();
    const totalDays = Math.floor(diffTime / (1000 * 60 * 60 * 24)) + 1;

    const pricePerDay = this.selectedEquipment.reduce((sum, item) => sum + (item.price || 0), 0);

    return pricePerDay * totalDays;
  }

  /**
   * Loads the reservation information and patches the form & equipment list.
   */
  loadReservation(): void {
    this.loading = true;
    this.reservationService.getById(this.reservationId).subscribe({
      next: (data: any) => {
        if (!data) {
          this.submitError = 'RESERVATION.NOT_FOUND';
          this.loading = false;
          return;
        }

        let formattedTime = data.pickUpTime || '09:00';
        if (formattedTime.length > 5) {
          formattedTime = formattedTime.substring(0, 5);
        }

        this.originalStartDate = data.startDate;
        this.originalEndDate = data.endDate;

        this.customerName = data.customerName;
        this.customerProfileId = data.customerProfileId;

        this.reservationForm.patchValue({
          startDate: data.startDate,
          pickUpTime: formattedTime,
          endDate: data.endDate
        });

        this.selectedEquipment = data.items || data.equipment || data.equipments || data.equipmentList || [];

        this.selectedEquipmentBackup = [...this.selectedEquipment];

        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load reservation details via getById', err);
        this.notification.error('Error loading reservation');
        this.submitError = this.errorMapping.getErrorMessage(err);
        this.loading = false;
      }
    });
  }

  /**
   * Starts the search within a specific equipment-type, using the search.function of equipment.service.ts.
   */
  openEquipmentSelection(type: EquipmentType | string): void {
    this.currentActiveType = type as EquipmentType;
    this.searchEquipment();
  }

  /**
   * Searches for a piece of equipment.
   */
  searchEquipment(): void {
    this.loading = true;

    const startDateString = this.reservationForm.get('startDate')?.value;
    const endDateString = this.reservationForm.get('endDate')?.value;

    const searchRequest: EquipmentSearch = {
      model: this.modelFilter.trim() || undefined,
      type: this.currentActiveType ?? undefined,
      targetSkillLevel: this.skillFilter ?? undefined,
      start: startDateString || undefined,
      end: endDateString || undefined
    };

    this.equipmentService.search(searchRequest).subscribe({
      next: (data) => {
        this.availableEquipmentList = this.sortByPrice(data);
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to search equipment', err);
        this.submitError = this.errorMapping.getErrorMessage(err);
        this.loading = false;
      }
    });
  }

  /**
   * Sorting-function for searchEquipment.
   */
  private sortByPrice(items: Equipment[]): Equipment[] {
    if (!this.priceSortDirection) {
      return items;
    }

    return [...items].sort((a, b) =>
      this.priceSortDirection === 'asc' ? a.price - b.price : b.price - a.price
    );
  }

  /**
   * Event-Handler for manual sorting in the UI.
   */
  onPriceSortChange(): void {
    this.availableEquipmentList = this.sortByPrice(this.availableEquipmentList);
  }

  /**
   * Reset of Filters.
   */
  clearFilters(): void {
    this.modelFilter = '';
    this.currentActiveType = null;
    this.skillFilter = null;
    this.priceSortDirection = 'asc';
    this.availableEquipmentList = [];
    this.filtersExpanded = false;
    this.searchEquipment();
  }

  /**
   * Toggles the visibility of the filter section in the UI.
   */
  toggleFilters(): void {
    this.filtersExpanded = !this.filtersExpanded;
  }

  /**
   * Counts the number of active filters for display in the UI.
   */
  get activeFilterCount(): number {
    let count = 0;
    if (this.modelFilter?.trim()) count++;
    if (this.skillFilter) count++;
    if (this.priceSortDirection && this.priceSortDirection !== 'asc') count++;
    return count;
  }

  /**
   * Checks if a specific piece of equipment is already selected for the reservation.
   * @param itemId ID of the equipment to check
   * @returns true if the equipment is already selected, false otherwise
   */
  isAlreadySelected(itemId: number): boolean {
    return this.selectedEquipment.some(e => e.id === itemId);
  }

  /**
   * Toggles the visibility of the available equipment list in the UI.
   */
  toggleEquipmentList(): void {
    this.equipmentListExpanded = !this.equipmentListExpanded;
  }

  /**
   * Adds chosen piece of equipment to reservation.
   */
  addEquipment(item: Equipment): void {
    if (!this.selectedEquipment.some(e => e.id === item.id)) {
      this.selectedEquipment.push(item);
      this.selectedEquipmentBackup.push(item);
    }
  }

  /**
   * Removes chosen piece of equipment from reservation.
   */
  removeEquipment(itemId: number): void {
    this.selectedEquipment = this.selectedEquipment.filter(item => item.id !== itemId);
    this.selectedEquipmentBackup = this.selectedEquipmentBackup.filter(item => item.id !== itemId);

    const start = this.reservationForm.get('startDate')?.value;
    const end = this.reservationForm.get('endDate')?.value;
    if (start && end && !this.isDateRangeInvalid) {
      this.validateSelectedEquipmentForNewDates(start, end);
    }
  }

  /**
   * Cancels the reservation.
   */
  cancel(): void {
    this.router.navigate(['/staff/reservation']);
  }

  // Helper-Method to connect HTML to Confirmation-Pop-Up.
  confirmAndSubmit(dialogElement: HTMLDialogElement): void {
    dialogElement.close();
    this.submitReservation();
  }

  /**
   * Submits finished reservation to backend.
   */
  submitReservation(): void {
    if (this.reservationForm.invalid) {
      return;
    }

    if (this.selectedEquipment.length === 0) {
      this.notification.warning('Please choose at least 1 piece of equipment');
      return;
    }

    if (this.isDateRangeInvalid) {
      this.notification.error('The return date cannot be before the pick-up date');
      return;
    }

    const formValue = this.reservationForm.value;
    this.submitLoading = true;
    this.submitError = null;

    const reservationPayload: ReservationUpdate = {
      id: this.reservationId,
      customerProfileId: this.customerProfileId,
      equipmentIds: this.selectedEquipment.map(e => e.id),
      pickUpTime: formValue.pickUpTime.length === 5 ? formValue.pickUpTime + ':00' : formValue.pickUpTime,
      startDate: formValue.startDate,
      endDate: formValue.endDate
    };

    this.reservationService.updateForStaff(this.reservationId, reservationPayload).subscribe({
      next: (response) => {
        console.log('Reservation updated successfully', response);
        this.submitLoading = false;
        this.router.navigate(['/staff/reservation']);
        this.notification.success(this.translateService.instant('RESERVATION.EDIT_SUCCESS'));
      },
      error: (err) => {
        console.error('Error during update of reservation', err);
        this.submitLoading = false;
        this.submitError = this.errorMapping.getErrorMessage(err);
      }
    });
  }
}
