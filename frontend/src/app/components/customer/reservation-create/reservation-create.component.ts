import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ReservationService } from '../../../services/reservation.service';
import { EquipmentService } from '../../../services/equipment.service';
import { ReservationCreation } from '../../../dtos/reservation-creation';
import { Equipment } from '../../../dtos/equipment';
import { EquipmentSearch } from '../../../dtos/equipment-search';
import { CustomerProfile } from '../../../dtos/customer-profile';
import {EquipmentType} from "../../../dtos/equipmenttype";
import {RentalStatus} from "../../../dtos/rentalstatus";
import {SkillLevel} from "../../../dtos/skilllevel";
import {debounceTime, distinctUntilChanged} from "rxjs";
import {CustomerProfileService} from "../../../services/customer-profile.service";

@Component({
  selector: 'app-reservation-create',
  templateUrl: './reservation-create.component.html',
  styleUrls: ['./reservation-create.component.scss'],
  standalone: false
})
export class ReservationCreateComponent implements OnInit {

  reservationForm!: FormGroup;

  //To give HTML access to Equipment-Type-Enum
  readonly EquipmentTypeEnum = EquipmentType;

  selectedEquipment: Equipment[] = [];
  availableEquipmentList: Equipment[] = [];
  customerProfiles: CustomerProfile[] = [];

  currentActiveType: EquipmentType | null = null;
  modelFilter: string ='';
  statusFilter: RentalStatus | null = null;
  skillFilter: SkillLevel | null = null;
  priceSortDirection: 'asc' | 'desc' | '' = 'asc';

  loading: boolean = false;
  submitLoading: boolean = false;
  submitError: string | undefined = undefined;
  validationWarning: string | undefined = undefined;

  constructor(
    private fb: FormBuilder,
    private reservationService: ReservationService,
    private equipmentService: EquipmentService,
    private customerProfileService: CustomerProfileService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.initDateChangeWatcher();
    this.loadCustomerProfiles();
  }

  /**
   * Initializes the form with validators confirming to the ones used in the backend.
   */
  private initForm(): void {
    this.reservationForm = this.fb.group({
      customerProfileId: [null, Validators.required],
      pickUpDate: [null, Validators.required],
      pickUpTime: ['09:00', Validators.required],
      returnDate: [null, Validators.required]
    });

    this.reservationForm.get('customerProfileId')?.valueChanges.subscribe(profileId => {
      if (profileId) {
        this.applyProfileFilters(profileId);
      }
    });

    this.reservationForm.valueChanges.subscribe(values => {
      if(this.currentActiveType && values.pickUpDate && values.returnDate && !this.isDateRangeInvalid){
        this.searchEquipment();
      }
    })
  }

  /**
   * Loads genuine customer profiles from backend for the hardcoded customer ID 1.
   * TODO: Remove hard-coding this to 1 once proper Accounts with AccoundId exist
   */
  private loadCustomerProfiles(): void {
    const hardcodedCustomerId = 1;
    this.customerProfileService.getCustomerProfiles(hardcodedCustomerId).subscribe({
      next: (profiles) => {
        this.customerProfiles = profiles;
        if (this.customerProfiles.length > 0) {
          this.reservationForm.patchValue({ customerProfileId: this.customerProfiles[0].id });
        }
      },
      error: (err) => {
        console.error('Failed to load customer profiles from backend', err);
        this.submitError = "RESERVATION.CUSTOMER_PROFILES_LOADING_FAILED";
      }
    });
  }

  /**
   * Watches for changes in pickUpDate or returnDate to re-validate already selected equipment.
   */
  private initDateChangeWatcher(): void {
    this.reservationForm.valueChanges.pipe(
        debounceTime(300),
        distinctUntilChanged((prev, curr) =>
            prev.pickUpDate === curr.pickUpDate && prev.returnDate === curr.returnDate
        )
    ).subscribe(values => {
      if (this.selectedEquipment.length > 0 && values.pickUpDate && values.returnDate && !this.isDateRangeInvalid) {
        this.validateSelectedEquipmentForNewDates(values.pickUpDate, values.returnDate);
      }
    });
  }

  /**
   * Validates already selected equipment against a newly chosen date range.
   * Removes equipment that is no longer available.
   */
  private validateSelectedEquipmentForNewDates(startDate: string, endDate: string): void {
    const searchRequest: EquipmentSearch = {
      start: startDate,
      end: endDate
    };

    this.equipmentService.search(searchRequest).subscribe({
      next: (availableEquipment) => {
        const availableIds = availableEquipment.map(e => e.id);

        const filteredList = this.selectedEquipment.filter(item => availableIds.includes(item.id));

        if (filteredList.length !== this.selectedEquipment.length) {
          this.selectedEquipment = filteredList;
          this.validationWarning = 'RESERVATION.VALIDATION_WARNING';

          setTimeout(() => this.validationWarning = undefined, 8000);
        } else {
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
    const start = this.reservationForm.get('pickUpDate')?.value;
    const end = this.reservationForm.get('returnDate')?.value;
    if(!start || !end){
      return false;
    }
    return new Date(end) < new Date(start);
  }

  /**
   * Calculates the rentageDurationDays.
   * Pick-Up and Return on same day counts as 1 day.
   */
  calculateRentDuration(): number {
    const start = this.reservationForm.get('pickUpDate')?.value;
    const end = this.reservationForm.get('returnDate')?.value;

    if (!start || !end) {
      return 0;
    }

    const startDate = new Date(start);
    const endDate = new Date(end);

    const diffTime = endDate.getTime() - startDate.getTime();
    if (diffTime < 0) {
      return 0;
    }

    return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
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

    const startDateString = this.reservationForm.get('pickUpDate')?.value;
    const endDateString = this.reservationForm.get('returnDate')?.value;

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
  }

  /**
   * Adds chosen piece of equipment to reservation.
   */
  addEquipment(item: Equipment): void {
    if (!this.selectedEquipment.some(e => e.id === item.id)) {
      this.selectedEquipment.push(item);
    }
  }

  /**
   * Removes chosen piece of equipment from reservation.
   */
  removeEquipment(itemId: number): void {
    this.selectedEquipment = this.selectedEquipment.filter(item => item.id !== itemId);
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
   * Submits finished reservation to backend.
   */
  submitReservation(): void {
    if (this.reservationForm.invalid) {
      return;
    }

    if (this.selectedEquipment.length === 0) {
      //Also caught in HTML, but here again just to be sure.
      alert('Please choose at least 1 piece of equipment');
      return;
    }

    const duration = this.calculateRentDuration();
    if (duration <= 0) {
      alert('The return date cannot be before the pick-up date');
      return;
    }

    // Confirmation Pop-Up before submission
    const hasConfirmed = window.confirm('Are you sure you want to submit this reservation?');
    if (!hasConfirmed) {
      return;
    }

    const formValue = this.reservationForm.value;

    //Creation of DTO
    const reservationPayload: ReservationCreation = {
      customerProfileId: formValue.customerProfileId,
      equipmentIds: this.selectedEquipment.map(e => e.id),
      pickUpTime: formValue.pickUpTime + ':00',
      pickUpDate: formValue.pickUpDate,
      rentDurationDays: duration
    };

    //Service-Call
    this.submitLoading = true;
    this.submitError = undefined;
    this.reservationService.create(reservationPayload).subscribe({
      next: (response) => {
        console.log('Reservation submitted successfully', response);
        this.submitLoading = false;
        this.router.navigate(['/customer']);
      },
      error: (err) => {
        console.error('Error during submission of reservation', err);
        this.submitLoading = false;
        this.submitError = err.error?.message || 'An error occurred while creating the reservation.';
      }
    });
  }
}
