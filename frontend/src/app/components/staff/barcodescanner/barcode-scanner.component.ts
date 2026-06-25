import {Component, OnInit} from '@angular/core';
import {EquipmentService} from '../../../services/equipment.service';
import {ReservationService} from '../../../services/reservation.service';
import {BarcodeScannerService} from '../../../services/barcode-scanner.service';
import {TranslateService} from '@ngx-translate/core';
import {Equipment} from '../../../dtos/equipment';
import {ReservationSearch} from '../../../dtos/reservation-search';
import {ReservationDetail} from "../../../dtos/reservation-detail";
import {ReservationUpdate} from "../../../dtos/reservation-update";
import {ReservationStatus} from "../../../dtos/reservationstatus";
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {CustomerProfileService} from '../../../services/customer-profile.service';
import {CustomerProfile} from "../../../dtos/customer-profile";
import {EquipmentType} from "../../../dtos/equipmenttype";
import {ToastrService} from "ngx-toastr";
import {ReservationCreationWithMode} from "../../../dtos/reservation-creation-with-mode";
import {StaffService} from '../../../services/staff.service';
import {CustomerSearch} from '../../../dtos/customer-search';
import {CustomerSearchResponse} from '../../../dtos/customer-search-response';
import {BarcodeFormat} from '@zxing/library';

@Component({
  selector: 'app-barcode-scanner',
  templateUrl: './barcode-scanner.component.html',
  styleUrl: './barcode-scanner.component.scss',
  standalone: false
})
export class BarcodeScannerComponent implements OnInit {
  inputBarcodeId = '';

  scannedEquipments: Equipment[] = [];
  scannedEquipmentIds: number[] = [];
  matchedReservations: ReservationDetail[] = [];
  pendingCustomerName = '';

  scanScenario: 'NO_RESERVATION' | 'SINGLE_RESERVATION' | 'CONFLICT_RESERVATION' = 'NO_RESERVATION';
  equipmentScenario: 'ALL_RESERVED_EQUIPMENT_SCANNED' | 'SOME_RESERVED_EQUIPMENT_SCANNED'
    | 'EQUIPMENT_SCANNED_THAT_IS_UNRESERVED' = 'SOME_RESERVED_EQUIPMENT_SCANNED';

  activeErrors: string[] = [];

  loading = false;
  errorMessage = '';
  successMessage = '';

  walkInForm!: FormGroup;
  allUsers: CustomerSearchResponse[] = [];
  filteredProfiles: CustomerProfile[] = [];
  submitLoading = false;
  submitError: string | null = null;

  //Mode for the walk-in checkout: RENTAL (default) or MAINTENANCE. Only relevant when
  //scanScenario === 'NO_RESERVATION', since that's the only scenario where a walk-in
  //(and therefore a maintenance send-off) makes sense.
  checkoutMode: 'RENTAL' | 'MAINTENANCE' = 'RENTAL';
  maintenanceCustomerProfileId: number | null = null;
  maintenanceLookupError: string | null = null;
  pendingCheckoutModeTarget: 'RENTAL' | 'MAINTENANCE' | null = null;

  isCameraOpen = true;
  isScanningPaused = false;
  allowedFormats = [BarcodeFormat.CODE_128];

  readonly EquipmentTypeEnum = EquipmentType;

  constructor(
    private equipmentService: EquipmentService,
    private reservationService: ReservationService,
    private barcodeScannerService: BarcodeScannerService,
    public translateService: TranslateService,
    private customerProfileService: CustomerProfileService,
    private staffService: StaffService,
    private fb: FormBuilder,
    private notification: ToastrService
  ) {
  }

  async ngOnInit() {
    window.scrollTo(0, 0);
    this.initWalkInForm();
    this.loadAllSystemUsers();
    navigator.mediaDevices?.addEventListener(
      'devicechange',
      this.handleDeviceChange
    );
  }

  //----------------------------------------------------------------------------------------------------------
  //Scanner-Code

  openCamera() {
    this.isCameraOpen = true;
  }

  closeCamera(): void {
    this.isCameraOpen = false;
  }

  onCodeResult(result: string): void {
    if (this.isScanningPaused) {
      return;
    }
    this.isScanningPaused = true;
    this.inputBarcodeId = result;
    console.log('Successfully scanned code:', result);
    this.searchEquipment();

    //Disable scanning for 2 seconds to avoid multiple scanning of the same code
    setTimeout(() => {
      this.isScanningPaused = false;
    }, 1000);
  }

  onCameraError(err: any): void {
    console.error("Camera error:", err)
    this.notification.error(this.translateService.instant('BARCODE_SCANNER.CAMERA_ERROR'));
  }

  onNoCameraFound(): void {
    console.warn('No camera hardware detected.');
    this.notification.error(this.translateService.instant('BARCODE_SCANNER.NO_CAMERA_FOUND'));
    this.closeCamera();
  }

  private handleDeviceChange = async () => {
    const devices = await navigator.mediaDevices.enumerateDevices();

    const videoInputs = devices.filter(
      d => d.kind === 'videoinput'
    );

    if (videoInputs.length === 0) {
      console.warn('Camera disconnected');
      this.closeCamera();
    }
  };

  ngOnDestroy() {
    navigator.mediaDevices?.removeEventListener(
      'devicechange',
      this.handleDeviceChange
    );
  }

  //----------------------------------------------------------------------------------------------------------
  //General Business-Logic of Barcode-Scanner

  //Searches for equipment corresponding to the input barcodeId (using the getEquipmentByBarcodeId-method,
  //not the search-method of equipmentservice)
  //Also searches for reservations corresponding to the found equipment, so reservations that start today
  //and include a scanned piece of equipment
  searchEquipment(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.activeErrors = [];

    const barcode = this.inputBarcodeId.trim();
    if (!barcode) {
      this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_EMPTY');
      this.updateScanScenario();
      return;
    }

    this.loading = true;

    //Search for equipment fitting to barcode
    this.equipmentService.getByBarcodeId(barcode).subscribe({
      next: (equipmentData) => {
        if (equipmentData) {
          const alreadyExists = this.scannedEquipmentIds.includes(equipmentData.id);

          if (alreadyExists) {
            this.notification.error(this.translateService.instant('BARCODE_SCANNER.ERROR_ALREADY_SCANNED', {model: equipmentData.model}));
            this.loading = false;
            this.updateScanScenario();
            return;
          }

          //Maintenance mode only ever allows exactly one equipment item at a time: maintenance
          //reservations bundle into a single Reservation, and reactivation can only return that
          //whole Reservation at once (no partial multi-item return support). A second item would
          //either get force-bundled into the same maintenance batch, or block reactivation of an
          //already-fixed item until ALL items in the batch are ready - neither is acceptable.
          if (this.checkoutMode === 'MAINTENANCE' && this.scannedEquipmentIds.length >= 1) {
            this.errorMessage = 'BARCODE_SCANNER.MAINTENANCE_ERROR_ONLY_ONE_ITEM';
            this.loading = false;
            this.updateScanScenario();
            return;
          }

          this.scannedEquipments.push(equipmentData);
          this.scannedEquipmentIds.push(equipmentData.id);

          this.notification.success(this.translateService.instant('BARCODE_SCANNER.SUCCESS_ADDED', {model: equipmentData.model}) || "Scanned successfully.");
          this.successMessage = this.translateService.instant('BARCODE_SCANNER.SUCCESS_ADDED', {model: equipmentData.model});
          this.inputBarcodeId = '';

          setTimeout(() => {
            this.successMessage = '';
          }, 3000);

          //Date-Format YYYY-MM-DD; Swedish timezone because they are in the same one as Austria (and have same
          // summertime-rulings due to EU-rulings), but also have the Date-Format we use in the rest of the application
          const todayIsoDate = new Date().toLocaleDateString('sv-SE');

          const searchParams: ReservationSearch = {
            equipmentIds: [equipmentData.id],
            startDate: todayIsoDate
          };

          //Search for Reservations that are today and correspond to equipment that was scanned
          this.reservationService.search(searchParams).subscribe({
            next: (reservations) => {
              this.loading = false;

              if (reservations && reservations.length > 0) {
                console.log('Found fitting Reservations', reservations);

                let hasValidActiveReservation = reservations.some(reservation =>
                  reservation.reservationStatus === ReservationStatus.CREATED ||
                  reservation.reservationStatus === ReservationStatus.PICKED_UP
                );

                if (this.walkInForm && hasValidActiveReservation) {
                  this.walkInForm.reset();
                  this.initWalkInForm();
                }

                reservations.forEach(reservation => {
                  const isValidStatus = reservation.reservationStatus === ReservationStatus.CREATED ||
                    reservation.reservationStatus === ReservationStatus.PICKED_UP;

                  if (isValidStatus) {
                    const exists = this.matchedReservations.some(r => r.id === reservation.id);
                    if (!exists) {
                      this.matchedReservations.push(reservation);
                    }
                  }
                });

                if (this.matchedReservations.length > 0) {
                  this.successMessage = this.translateService.instant(
                    'BARCODE_SCANNER.SUCCESS_RESERVATION_FOUND', {model: equipmentData.model}
                  );
                  setTimeout(() => this.successMessage = '', 3000);

                  this.updateScanScenario();
                } else {
                  this.handleNoActiveReservationFound();
                }
              }
                //Only offer creation of new Reservation if no Reservation is found and no Reservation has been found
              // for other equipment
              else if (this.matchedReservations.length == 0) {
                this.handleNoActiveReservationFound();
              }

              this.updateScanScenario();
            },
            error: (err) => {
              console.error('Error during the Reservation Search', err);
              this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_UNEXPECTED');
              this.loading = false;
              this.updateScanScenario();
            }
          });
        } else {
          this.loading = false;
        }
      },
      error: (err) => {
        console.error('Failed to search equipment by barcode', err);
        this.loading = false;
        if (err.status === 404) {
          this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_NOT_FOUND', {value: barcode});
        } else {
          this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_UNEXPECTED');
        }
        this.updateScanScenario();
      }
    });
  }

  //Helper-Method for searchEquipment that handles the case that there were no active Reservations found for the
  // scanned pieces of Equipment
  private handleNoActiveReservationFound(): void {
    console.log('No active reservations (CREATED/PICKED_UP) found for this equipment today.');
    if (!this.walkInForm) {
      this.initWalkInForm();
    }
  }

  //Removes the equipment, but only from the temporary scan-list - not from the database itself!
  removeEquipmentFromList(item: Equipment): void {
    this.errorMessage = '';
    this.scannedEquipments = this.scannedEquipments.filter(e => e.id !== item.id);
    this.scannedEquipmentIds = this.scannedEquipmentIds.filter(id => id !== item.id);

    this.successMessage = this.translateService.instant('BARCODE_SCANNER.SUCCESS_REMOVED', {model: item.model});
    setTimeout(() => {
      this.successMessage = '';
    }, 3000);

    //Filter out Reservations that have no more matching Equipment
    this.matchedReservations = this.matchedReservations.filter(res => {
      return res.items?.some((equipment: any) => this.scannedEquipmentIds.includes(equipment.id));
    })

    this.updateScanScenario();
  }

  //Helper-Method that changes the current mode depending on how many corresponding reservations to the equipment
  // were found (more than 1 reservation that corresponds to equipment prevents submission of scan-check-in/out)
  //Also changes the current mode if there is equipment not corresponding to a Reservation
  updateScanScenario(): void {
    this.activeErrors = [];
    //Save existing errorMessages in activeErrors
    if (this.errorMessage) {
      this.activeErrors.push(this.errorMessage);
    }
    const count = this.matchedReservations.length;

    //Part 1: Resolve mode from conflicts with Reservations
    if (count === 0) {
      this.scanScenario = 'NO_RESERVATION';
      if (this.scannedEquipments.length > 0 && this.checkoutMode === 'RENTAL') {
        this.activeErrors.push(
          this.translateService.instant('BARCODE_SCANNER.ERROR_NO_RESERVATION_TODAY', {
            model: this.scannedEquipments[this.scannedEquipments.length - 1].model
          })
        );
      }
    } else if (count === 1) {
      this.scanScenario = 'SINGLE_RESERVATION';
    } else {
      this.scanScenario = 'CONFLICT_RESERVATION';
      this.activeErrors.push('BARCODE_SCANNER.SCENARIO_CONFLICT_WARN');
    }

    //Part 1b: Maintenance mode only makes sense for equipment that's either unreserved
    //(NO_RESERVATION) or already in MAINTENANCE (reactivation - scanning it again to bring
    //it back). A genuine conflict is when a REAL customer reservation exists, i.e. the
    //equipment's own status is something other than MAINTENANCE (e.g. RENTED or FREE-but-
    //booked-for-today) while we're in maintenance mode. We check the equipment's status
    //rather than scanScenario alone, since our own maintenance "reservation" also shows up
    //as an active reservation today, and that one must NOT be blocked.
    if (this.checkoutMode === 'MAINTENANCE' && this.scanScenario !== 'NO_RESERVATION') {
      const hasGenuineConflict = this.scannedEquipments.some(eq => eq.status !== 'MAINTENANCE');
      if (hasGenuineConflict) {
        this.activeErrors.push('BARCODE_SCANNER.MAINTENANCE_WARN_HAS_RESERVATION');
      }
    }

    //Part 2: Resolve mode from conflicts with Equipment not belonging to Reservations
    const allReservedIds = this.matchedReservations.reduce((ids: string[], res) => {
      const itemIds = res.items?.map((eq: any) => eq.id) || [];
      return [...ids, ...itemIds];
    }, []);

    const hasUnreserved = this.scannedEquipmentIds.some(id => !allReservedIds.includes(id));

    if (count > 0 && hasUnreserved) {
      this.equipmentScenario = 'EQUIPMENT_SCANNED_THAT_IS_UNRESERVED';
      this.activeErrors.push('BARCODE_SCANNER.SCENARIO_UNRESERVED_WARN');
    } else if (count == 1) {
      const targetReservation = this.matchedReservations[0];
      const hasMissingEquipment = targetReservation.items?.some(
        (eq: any) => !this.scannedEquipmentIds.includes(eq.id)
      );

      if (hasMissingEquipment) {
        this.equipmentScenario = 'SOME_RESERVED_EQUIPMENT_SCANNED';
        this.activeErrors.push('BARCODE_SCANNER.SCENARIO_SOME_RESERVED_WARN');
      } else {
        this.equipmentScenario = 'ALL_RESERVED_EQUIPMENT_SCANNED';
      }
    } else {
      this.equipmentScenario = 'SOME_RESERVED_EQUIPMENT_SCANNED';
    }
  }

  //Submits the updated Reservation and does the checkout when there is already a Reservation
  submitExistingReservation(): void {
    if (this.hasGenuineMaintenanceConflict) {
      return;
    }
    if (this.scanScenario !== 'SINGLE_RESERVATION' || this.equipmentScenario !== 'ALL_RESERVED_EQUIPMENT_SCANNED') {
      return;
    }

    const currentReservation = this.matchedReservations[0];
    let nextStatus: ReservationStatus.CREATED | ReservationStatus.PICKED_UP | ReservationStatus.RETURNED;

    if (currentReservation.reservationStatus === ReservationStatus.CREATED) {
      nextStatus = ReservationStatus.PICKED_UP;
    } else if (currentReservation.reservationStatus === ReservationStatus.PICKED_UP) {
      nextStatus = ReservationStatus.RETURNED;
    } else {
      return;
    }

    const updateDto: ReservationUpdate = {
      id: currentReservation.id,
      reservationStatus: nextStatus,
      equipmentIds: this.scannedEquipmentIds
    };

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.barcodeScannerService.checkOutOrInScanWithExistingReservation(updateDto).subscribe({
      next: (updatedReservation) => {
        this.loading = false;
        this.successMessage = this.translateService.instant('BARCODE_SCANNER.SUCCESS_PROCESS_COMPLETED');

        this.scannedEquipments = [];
        this.scannedEquipmentIds = [];
        this.matchedReservations = [];
        this.inputBarcodeId = '';
        this.updateScanScenario();
      },
      error: (err) => {
        this.loading = false;
        console.error('Error during rental update submission', err);
        this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_UNEXPECTED');
        this.updateScanScenario();
      }
    });
  }

  /**
   * Adds a specific equipment item directly to the current matched reservation (if a Reservation was loaded from
   * the Backend).
   */
  addEquipmentToExistingReservation(equipmentId: number): void {
    if (this.matchedReservations.length !== 1) return;

    const currentReservation = this.matchedReservations[0];
    const dto = {
      id: currentReservation.id,
      equipmentIds: [equipmentId]
    };

    this.loading = true;
    this.reservationService.addEquipmentToReservation(dto).subscribe({
      next: (updatedReservation) => {
        this.loading = false;
        this.matchedReservations[0] = updatedReservation;
        this.notification.success(this.translateService.instant('BARCODE_SCANNER.SUCCESS_EQUIPMENT_ADDED_TO_RES') || 'Equipment added to reservation.');

        this.updateScanScenario();
      },
      error: (err) => {
        this.loading = false;
        console.error('Failed to add equipment to reservation', err);
        this.notification.error(err.error?.message || 'Error adding equipment.');
      }
    });
  }

  /**
   * Removes a specific equipment item directly from the current matched reservation (if a Reservation was loaded
   * from the Backend).
   */
  removeEquipmentFromExistingReservation(equipmentId: number): void {
    if (this.matchedReservations.length !== 1) return;

    const currentReservation = this.matchedReservations[0];
    const dto = {
      id: currentReservation.id,
      equipmentIds: [equipmentId]
    };

    this.loading = true;
    this.reservationService.removeEquipmentFromReservation(dto).subscribe({
      next: (updatedReservation) => {
        this.loading = false;
        this.matchedReservations[0] = updatedReservation;
        this.notification.success(this.translateService.instant('BARCODE_SCANNER.SUCCESS_EQUIPMENT_REMOVED_FROM_RES') || 'Equipment removed from reservation.');

        this.scannedEquipments = this.scannedEquipments.filter(e => e.id !== equipmentId);
        this.scannedEquipmentIds = this.scannedEquipmentIds.filter(id => id !== equipmentId);

        this.updateScanScenario();
      },
      error: (err) => {
        this.loading = false;
        console.error('Failed to remove equipment from reservation', err);
        this.notification.error(err.error?.message || 'Error removing equipment.');
      }
    });
  }

  isEquipmentUnreserved(equipmentId: number): boolean {
    if (!this.matchedReservations || this.matchedReservations.length === 0) {
      return false;
    }
    return !this.matchedReservations[0].items?.some(eq => eq.id === equipmentId);
  }

  //Initializes the WalkInForm with current date and time as startDate and pickUpTime
  private initWalkInForm(): void {
    const today = new Date().toLocaleDateString('sv-SE');
    const nowTime = new Date().toTimeString().substring(0, 5);

    this.walkInForm = this.fb.group({
      userId: [null, Validators.required],
      customerProfileId: [{value: null, disabled: true}, Validators.required],
      startDate: [{value: today, disabled: true}, Validators.required],
      pickUpTime: [{value: nowTime, disabled: true}, Validators.required],
      endDate: [null, Validators.required]
    });

    //Checks whether a CustomerAccount is selected, to load the corresponding profiles
    this.walkInForm.get('userId')?.valueChanges.subscribe(userId => {
      if (userId) {
        this.loadProfilesForUser(userId);
      } else {
        this.walkInForm.get('customerProfileId')?.disable();
        this.filteredProfiles = [];
      }
    });
  }

  //True only when a REAL customer reservation conflicts with maintenance mode - i.e. at least
  //one scanned item's status is something other than MAINTENANCE while a reservation exists
  //for it today. Does NOT fire for our own maintenance "reservation" (status === MAINTENANCE),
  //since scanning a maintenance item again is the legitimate reactivation flow.
  get hasGenuineMaintenanceConflict(): boolean {
    return this.checkoutMode === 'MAINTENANCE'
      && this.scanScenario !== 'NO_RESERVATION'
      && this.scannedEquipments.some(eq => eq.status !== 'MAINTENANCE');
  }

  //True when in maintenance mode and the scanned item is itself already in MAINTENANCE -
  //i.e. we're looking at a reactivation case (bringing it back to FREE), not a conflict.
  get isMaintenanceReactivation(): boolean {
    return this.checkoutMode === 'MAINTENANCE'
      && this.scanScenario === 'SINGLE_RESERVATION'
      && this.scannedEquipments.length > 0
      && this.scannedEquipments.every(eq => eq.status === 'MAINTENANCE');
  }

  //----------------------------------------------------------------------------------------------------------
  //Maintenance-Mode-Toggle (only relevant within the walk-in / NO_RESERVATION scenario)

  //Called by the toggle in the template. Shows a confirmation dialog if items are already
  //scanned (since switching mode would clear them), otherwise switches immediately.
  requestCheckoutModeSwitch(target: 'RENTAL' | 'MAINTENANCE', dialogElement: HTMLDialogElement): void {
    if (target === this.checkoutMode) {
      return;
    }

    if (this.scannedEquipmentIds.length > 0) {
      this.pendingCheckoutModeTarget = target;
      dialogElement.showModal();
    } else {
      this.applyCheckoutModeSwitch(target);
    }
  }

  //Called when the user confirms the mode-switch warning dialog
  confirmCheckoutModeSwitch(dialogElement: HTMLDialogElement): void {
    dialogElement.close();
    if (this.pendingCheckoutModeTarget) {
      this.applyCheckoutModeSwitch(this.pendingCheckoutModeTarget);
      this.pendingCheckoutModeTarget = null;
    }
  }

  private applyCheckoutModeSwitch(target: 'RENTAL' | 'MAINTENANCE'): void {
    //Clear scanned items and related state, since switching mode invalidates the current batch
    this.scannedEquipments = [];
    this.scannedEquipmentIds = [];
    this.matchedReservations = [];
    this.errorMessage = '';
    this.successMessage = '';
    this.maintenanceLookupError = null;

    this.checkoutMode = target;
    this.updateScanScenario();

    if (target === 'MAINTENANCE' && this.maintenanceCustomerProfileId === null) {
      this.loadMaintenanceCustomerProfileId();
    }
  }

  //Looks up the fixed "Maintenance" customer account by its known email, then its one
  //CustomerProfile, so submitWalkInCheckout() has a customerProfileId to send when
  //checkoutMode === 'MAINTENANCE'. Runs once and caches the result.
  private loadMaintenanceCustomerProfileId(): void {
    const search: CustomerSearch = {email: 'maintenance@system.internal'};

    this.staffService.searchCustomers(search).subscribe({
      next: (customers) => {
        if (!customers || customers.length === 0) {
          this.maintenanceLookupError = this.translateService.instant('BARCODE_SCANNER.MAINTENANCE_ERROR_NO_ACCOUNT');
          return;
        }

        const maintenanceCustomerId = customers[0].id;
        if (maintenanceCustomerId === undefined) {
          this.maintenanceLookupError = this.translateService.instant('BARCODE_SCANNER.MAINTENANCE_ERROR_NO_ACCOUNT');
          return;
        }

        this.customerProfileService.getCustomerProfilesByCustomerId(maintenanceCustomerId).subscribe({
          next: (profiles) => {
            if (!profiles || profiles.length === 0) {
              this.maintenanceLookupError = this.translateService.instant('BARCODE_SCANNER.MAINTENANCE_ERROR_NO_PROFILE');
              return;
            }
            this.maintenanceCustomerProfileId = profiles[0].id;
          },
          error: (err) => {
            console.error('Failed to load maintenance customer profile', err);
            this.maintenanceLookupError = this.translateService.instant('BARCODE_SCANNER.ERROR_UNEXPECTED');
          }
        });
      },
      error: (err) => {
        console.error('Failed to load maintenance customer account', err);
        this.maintenanceLookupError = this.translateService.instant('BARCODE_SCANNER.ERROR_UNEXPECTED');
      }
    });
  }

  //Loads all customerAccounts for the dropdown
  private loadAllSystemUsers(): void {
    const search: CustomerSearch = {};

    this.staffService.searchCustomers(search).subscribe({
      next: (customers) => {
        if (customers) {
          this.allUsers = customers || [];
        } else {
          this.allUsers = [];
        }
      },
      error: (err) => {
        this.loading = false;
        console.error('Failed to load all system users:', err);
        this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_UNEXPECTED');
      }
    });
  }

  //Loads the customerProfiles for the selected customerAccount
  private loadProfilesForUser(userId: number): void {
    this.customerProfileService.getCustomerProfilesByCustomerId(userId).subscribe({
      next: (profiles) => {
        this.filteredProfiles = profiles;
        this.walkInForm.get('customerProfileId')?.enable();
        if (profiles.length > 0) {
          this.walkInForm.patchValue({customerProfileId: profiles[0].id});
        }
      },
      error: (err) => {
        console.error('Error during loading of customer profiles', err);
        this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_UNEXPECTED');
      }
    });
  }

  //Checks that the returnDate is correct
  get isWalkInDateRangeInvalid(): boolean {
    const end = this.walkInForm.get('endDate')?.value;
    if (!end) return false;
    const today = new Date().toLocaleDateString('sv-SE');
    return new Date(end) < new Date(today);
  }

  //Submits the Reservation and checkout as part of the Walk-In-Construct when there is no Reservation.
  //Also handles the maintenance walk-in when checkoutMode === 'MAINTENANCE'.
  submitWalkInCheckout(): void {
    if (!this.scannedEquipmentIds || this.scannedEquipmentIds.length === 0 || this.scannedEquipments.length === 0) {
      return;
    }

    if (this.checkoutMode === 'MAINTENANCE') {
      if (this.maintenanceCustomerProfileId === null || this.isWalkInDateRangeInvalid || !this.walkInForm.get('endDate')?.value) {
        return;
      }
    } else if (this.walkInForm.invalid || this.isWalkInDateRangeInvalid) {
      return;
    }

    this.submitLoading = true;
    this.submitError = null;

    const formValue = this.walkInForm.getRawValue();

    const payload: ReservationCreationWithMode = {
      customerProfileId: this.checkoutMode === 'MAINTENANCE'
        ? this.maintenanceCustomerProfileId!
        : formValue.customerProfileId,
      equipmentIds: this.scannedEquipmentIds,
      pickUpTime: formValue.pickUpTime + ':00',
      startDate: formValue.startDate,
      endDate: formValue.endDate,
      reservationStatus: ReservationStatus.PICKED_UP,
      mode: this.checkoutMode
    };

    this.barcodeScannerService.checkOutScanWithoutExistingReservation(payload).subscribe({
      next: (response) => {
        this.submitLoading = false;
        const successMsgKey = this.checkoutMode === 'MAINTENANCE'
          ? 'BARCODE_SCANNER.MAINTENANCE_SUCCESS_TOAST'
          : 'BARCODE_SCANNER.WALK_IN_SUCCESS_TOAST';
        this.notification.success(this.translateService.instant(successMsgKey));

        //UI-State-Reset
        this.scannedEquipments = [];
        this.scannedEquipmentIds = [];
        this.matchedReservations = [];
        this.inputBarcodeId = '';
        this.walkInForm.reset();
        this.initWalkInForm();
        this.updateScanScenario();
      },
      error: (err) => {
        console.error('Error during walk-in checkout submission', err);
        this.submitLoading = false;
        this.submitError = err.error?.message || 'An error occurred while creating the walk-in reservation.';
      }
    });
  }

  //Helper-Method for pop-up before submission
  openConfirmationModal(dialogElement: HTMLDialogElement): void {
    //Case 1: Checkout without Reservation (normal walk-in or maintenance)
    if (this.scanScenario === 'NO_RESERVATION') {
      if (!this.scannedEquipmentIds || this.scannedEquipmentIds.length === 0) {
        return;
      }

      if (this.checkoutMode === 'MAINTENANCE') {
        if (this.maintenanceCustomerProfileId === null || this.isWalkInDateRangeInvalid || !this.walkInForm.get('endDate')?.value) {
          return;
        }
      } else if (this.walkInForm.invalid || this.isWalkInDateRangeInvalid) {
        return;
      }

      this.pendingCustomerName = '';
      dialogElement.showModal();
      return;
    }

    //Case 2: Checkout with existing Reservation
    if (this.hasGenuineMaintenanceConflict) {
      return;
    }
    if (this.scanScenario !== 'SINGLE_RESERVATION' || this.equipmentScenario !== 'ALL_RESERVED_EQUIPMENT_SCANNED') {
      return;
    }
    this.pendingCustomerName = this.matchedReservations[0].customerName || '';
    dialogElement.showModal();
  }

  //Helper-method for pop-up before submission
  confirmAndSubmit(dialogElement: HTMLDialogElement): void {
    dialogElement.close();

    if (this.scanScenario === 'NO_RESERVATION') {
      this.submitWalkInCheckout();
    } else {
      this.submitExistingReservation();

    }
  }

  /**
   * Live total price calculation for the Walk-In / Spontaneous Rental form.
   */
  get walkInTotalPrice(): number {
    if (!this.walkInForm) return 0;

    const start = this.walkInForm.get('startDate')?.value;
    const end = this.walkInForm.get('endDate')?.value;

    if (!start || !end || this.isWalkInDateRangeInvalid || this.scannedEquipments.length === 0) {
      return 0;
    }

    const startDate = new Date(start);
    const endDate = new Date(end);

    const diffTime = endDate.getTime() - startDate.getTime();
    const totalDays = Math.floor(diffTime / (1000 * 60 * 60 * 24)) + 1;

    const pricePerDay = this.scannedEquipments.reduce((sum, item) => sum + (item.price || 0), 0);

    return pricePerDay * totalDays;
  }

  /**
   * Live total price for the scanned equipment block at the bottom.
   * Calculates the price dynamically based on today's date and the Walk-In end date if set.
   */
  get scannedItemsTotalPrice(): number {
    if (this.scannedEquipments.length === 0) return 0;

    let totalDays = 1; // Default fallback

    if (this.scanScenario === 'NO_RESERVATION' && this.walkInForm) {
      const start = this.walkInForm.get('startDate')?.value;
      const end = this.walkInForm.get('endDate')?.value;
      if (start && end && !this.isWalkInDateRangeInvalid) {
        const diffTime = new Date(end).getTime() - new Date(start).getTime();
        totalDays = Math.floor(diffTime / (1000 * 60 * 60 * 24)) + 1;
      }
    } else if (this.matchedReservations.length === 1) {
      // If matching an existing reservation, use its runtime
      const res = this.matchedReservations[0];
      if (res.startDate && res.endDate) {
        const diffTime = new Date(res.endDate).getTime() - new Date(res.startDate).getTime();
        totalDays = Math.floor(diffTime / (1000 * 60 * 60 * 24)) + 1;
      }
    }

    const pricePerDay = this.scannedEquipments.reduce((sum, item) => sum + (item.price || 0), 0);
    return pricePerDay * totalDays;
  }

  //Helper-method to give RentalStatus-Enum-Values nice background coloring in HTML
  getStatusClass(status: string): string {
    switch (status) {
      case 'FREE':
        return 'bg-success';
      case 'RESERVED':
        return 'bg-warning text-dark';
      case 'RENTED':
        return 'bg-danger';
      case 'MAINTENANCE':
        return 'bg-secondary';
      default:
        return 'bg-light text-dark';
    }
  }

  protected readonly ReservationStatus = ReservationStatus;
}
