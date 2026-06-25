import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ReservationService} from '../../../../services/reservation.service';
import {EquipmentService} from '../../../../services/equipment.service';
import {TranslateService} from '@ngx-translate/core';
import {ReservationCreation} from '../../../../dtos/reservation-creation';
import {Equipment} from '../../../../dtos/equipment';
import {EquipmentSearch} from '../../../../dtos/equipment-search';
import {CustomerProfile} from '../../../../dtos/customer-profile';
import {EquipmentType} from "../../../../dtos/equipmenttype";
import {RentalStatus} from "../../../../dtos/rentalstatus";
import {SkillLevel} from "../../../../dtos/skilllevel";
import {debounceTime, distinctUntilChanged} from "rxjs";
import {CustomerProfileService} from "../../../../services/customer-profile.service";
import {ReservationUpdate} from "../../../../dtos/reservation-update";
import {ToastrService} from 'ngx-toastr';
import {ReservationStatus} from "../../../../dtos/reservationstatus";
import {forkJoin, of} from 'rxjs';

export enum ReservationCreateEditMode {
  create,
  edit
}

@Component({
  selector: 'app-reservation-create',
  templateUrl: './reservation-create-edit.component.html',
  styleUrls: ['./reservation-create-edit.component.scss'],
  standalone: false
})
export class ReservationCreateEditComponent implements OnInit {

  readonly ReservationCreateEditMode = ReservationCreateEditMode;
  //To give HTML access to Equipment-Type-Enum
  readonly EquipmentTypeEnum = EquipmentType;

  mode: ReservationCreateEditMode = ReservationCreateEditMode.create;
  reservationId?: number;
  reservationForm!: FormGroup;

  selectedEquipment: Equipment[] = [];
  availableEquipmentList: Equipment[] = [];
  customerProfiles: CustomerProfile[] = [];

  currentActiveType: EquipmentType | null = null;
  modelFilter: string = '';
  statusFilter: RentalStatus | null = null;
  skillFilter: SkillLevel | null = null;
  priceSortDirection: 'asc' | 'desc' | '' = 'asc';

  filtersExpanded: boolean = false;
  equipmentListExpanded: boolean = true;

  loading: boolean = false;
  submitLoading: boolean = false;
  submitError: string | undefined = undefined;
  validationWarning: string | undefined = undefined;

  private originalStartDate!: string;
  private originalEndDate!: string;
  private selectedEquipmentBackup: Equipment[] = [];

  constructor(
    private fb: FormBuilder,
    private reservationService: ReservationService,
    private equipmentService: EquipmentService,
    private customerProfileService: CustomerProfileService,
    public translateService: TranslateService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService
  ) {
  }

  ngOnInit(): void {
    this.initForm();
    this.initDateChangeWatcher();

    if (this.route.snapshot.data['mode'] === ReservationCreateEditMode.edit) {
      this.mode = ReservationCreateEditMode.edit;
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.reservationId = Number(id);
        this.loadCustomerProfilesAndInit(this.reservationId);
      } else {
        this.mode = ReservationCreateEditMode.create;
        this.loadCustomerProfilesAndInit();
      }
    } else {
      this.mode = ReservationCreateEditMode.create;
      this.loadCustomerProfilesAndInit();
    }
  }

  /**
   * Initializes the form with validators confirming to the ones used in the backend.
   */
  private initForm(): void {
    this.reservationForm = this.fb.group({
      customerProfileId: [null, Validators.required],
      pickUpTime: ['09:00', Validators.required],
      startDate: [null, Validators.required],
      endDate: [null, Validators.required]
    });

    this.reservationForm.get('customerProfileId')?.valueChanges.subscribe(profileId => {
      if (profileId) {
        this.applyProfileFilters(profileId);
      }
    });

    this.reservationForm.valueChanges.subscribe(values => {
      if (this.currentActiveType && values.startDate && values.endDate && !this.isDateRangeInvalid) {
        this.searchEquipment();
      }
    })
  }

  /**
   * Loads genuine customer profiles from backend for the current customer.
   * Also checks whether we are editing an existing reservation.
   */
  private loadCustomerProfilesAndInit(editId?: number): void {
    this.customerProfileService.getCustomerProfiles().subscribe({
      next: (profiles) => {
        this.customerProfiles = profiles;

        if (this.mode === ReservationCreateEditMode.edit && editId) {
          this.loadExistingReservation(editId);
        } else {
          if (this.customerProfiles.length > 0) {
            this.reservationForm.patchValue({customerProfileId: this.customerProfiles[0].id});
          }
        }
      },
      error: (err) => {
        console.error('Failed to load customer profiles from backend', err);
        this.submitError = "RESERVATION.CUSTOMER_PROFILES_LOADING_FAILED";
      }
    });
  }

  /**
   * Loads the existing reservation information and patches the form & equipment list.
   */
  private loadExistingReservation(id: number): void {
    this.loading = true;

    this.reservationService.getById(id).subscribe({
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

        this.reservationForm.patchValue({
          customerProfileId: data.customerProfileId,
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
        this.submitError = 'RESERVATION.LOADING_FAILED';
        this.loading = false;
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
  private validateSelectedEquipmentForNewDates(startDate: string, endDate: string): void {
    // If the Modus is CREATE, just search for availability in the new date range
    if (this.mode === ReservationCreateEditMode.create) {
      const searchRequest: EquipmentSearch = { start: startDate, end: endDate };
      this.equipmentService.search(searchRequest).subscribe({
        next: (availableEquipment) => {
          const availableIds = availableEquipment.map(e => e.id);
          this.selectedEquipment = this.selectedEquipment.filter(item => availableIds.includes(item.id));
        }
      });
      return;
    }

    // If the Modus is EDIT, search for availability in the new date range but exclude the original date range
    // in that search so the Reservation doesn't block its own availability
    // Also, if we go back to the original date range or within that original date range, fill our selected Equipment
    // with the BackUp to restore Equipment "lost" to new dates

    const originalStart = new Date(this.originalStartDate);
    const originalEnd = new Date(this.originalEndDate);
    const newStart = new Date(startDate);
    const newEnd = new Date(endDate);

    // Case 1: New Date Range is entirely within original one - no new validation necessary, restore with Backup
    if (newStart >= originalStart && newEnd <= originalEnd) {
      this.selectedEquipment = [...this.selectedEquipmentBackup];
      this.validationWarning = undefined;
      return;
    }

    const requests = [];

    // Case 2: Complete change of startDate and endDate so that there is no more overlap with the original
    if (newEnd < originalStart || newStart > originalEnd) {
      requests.push(this.equipmentService.search({
        start: startDate,
        end: endDate
      }));
    }
    // Case 3: Partial Overlap with original date range - Calculation of the new days to check
    else {
      // newStartDate is earlier than originalStartDate -> search from newStartDate to the day before the originalStartDate
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
          availableIds = results[0].map(e => e.id);
        } else if (results.length === 2) {
          const idsFront = results[0].map(e => e.id);
          const idsBack = results[1].map(e => e.id);
          availableIds = idsFront.filter(id => idsBack.includes(id));
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
   * Starts the search within a specific equipment-type, using the search.function of equipment.service.ts.
   */
  openEquipmentSelection(type: EquipmentType | string): void {
    if (this.currentActiveType === type) {
      this.currentActiveType = null;
      return;
    }
    this.currentActiveType = type as EquipmentType;
    this.equipmentListExpanded = true;
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
      status: this.statusFilter ?? undefined,
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
    this.statusFilter = RentalStatus.FREE;
    this.skillFilter = null;
    this.priceSortDirection = 'asc';
    this.availableEquipmentList = [];
    this.filtersExpanded = false;
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
      this.selectedEquipment.unshift(item);
      this.selectedEquipmentBackup.push(item);
    }
  }

  /**
   * Removes chosen piece of equipment from reservation.
   */
  removeEquipment(itemId: number): void {
    this.selectedEquipment = this.selectedEquipment.filter(item => item.id !== itemId);
    this.selectedEquipmentBackup = this.selectedEquipmentBackup.filter(item => item.id !== itemId);
  }

  /**
   * Implements profile-dependent filters.
   */
  private applyProfileFilters(profileId: number): void {
    const selectedProfile = this.customerProfiles.find(p => p.id === profileId);

    if (selectedProfile) {
      this.skillFilter = selectedProfile.skillLevel;

      if (this.currentActiveType) {
        this.searchEquipment();
      }
    }
  }

  /**
   * Cancels the reservation.
   */
  cancel(): void {
    this.router.navigate(['/customer/reservation']);
  }

  //Helper-Method to connect HTML to Confirmation-Pop-Up.
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
      //Also caught in HTML, but here again just to be sure.
      this.notification.warning('Please choose at least 1 piece of equipment');
      return;
    }

    if (this.isDateRangeInvalid) {
      this.notification.error('The return date cannot be before the pick-up date');
      return;
    }

    const formValue = this.reservationForm.value;

    //Service-Call
    this.submitLoading = true;
    this.submitError = undefined;

    if (this.mode === ReservationCreateEditMode.create) {
      //Creation of Create-DTO
      const reservationPayload: ReservationCreation = {
        customerProfileId: formValue.customerProfileId,
        equipmentIds: this.selectedEquipment.map(e => e.id),
        pickUpTime: formValue.pickUpTime + ':00',
        startDate: formValue.startDate,
        endDate: formValue.endDate,
        reservationStatus: ReservationStatus.CREATED,
      };

      this.reservationService.create(reservationPayload).subscribe({
        next: (response) => {
          console.log('Reservation submitted successfully', response);
          this.submitLoading = false;
          this.router.navigate(['/customer/reservation']);
          const translatedMessage = this.translateService.instant('RESERVATION.CREATE_SUCCESS');
          this.notification.success(translatedMessage);
        },
        error: (err) => {
          console.error('Error during submission of reservation', err);
          this.submitLoading = false;
          this.submitError = err.error?.message || 'An error occurred while creating the reservation.';
        }
      });
    } else {
      //Creation of Update-DTO
      const reservationPayload: ReservationUpdate = {
        id: this.reservationId!,
        customerProfileId: formValue.customerProfileId,
        equipmentIds: this.selectedEquipment.map(e => e.id),
        pickUpTime: formValue.pickUpTime.length === 5 ? formValue.pickUpTime + ':00' : formValue.pickUpTime,
        startDate: formValue.startDate,
        endDate: formValue.endDate
      };

      this.reservationService.update(this.reservationId!, reservationPayload).subscribe({
        next: (response) => {
          console.log('Reservation updated successfully', response);
          this.submitLoading = false;
          this.router.navigate(['/customer/reservation']);
          this.notification.success(this.translateService.instant('RESERVATION.EDIT_SUCCESS'));
        },
        error: (err) => {
          console.error('Error during update of reservation', err);
          this.submitLoading = false;
          this.submitError = err.error?.message || 'An error occurred while updating the reservation.';
        }
      });
    }
  }
}
