import {Component} from '@angular/core';
import {EquipmentService} from '../../../services/equipment.service';
import {ReservationService} from '../../../services/reservation.service';
import {BarcodeScannerService} from '../../../services/barcode-scanner.service';
import {TranslateService} from '@ngx-translate/core';
import {Equipment} from '../../../dtos/equipment';
import {ReservationSearch} from '../../../dtos/reservation-search';
import {ReservationDetail} from "../../../dtos/reservation-detail";
import {ReservationUpdate} from "../../../dtos/reservation-update";
import {ReservationStatus} from "../../../dtos/ReservationStatus";

@Component({
  selector: 'app-barcode-scanner',
  templateUrl: './barcode-scanner.component.html',
  styleUrl: './barcode-scanner.component.scss',
  standalone: false
})
export class BarcodeScannerComponent {
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

  constructor(
    private equipmentService: EquipmentService,
    private reservationService: ReservationService,
    private barcodeScannerService: BarcodeScannerService,
    public translateService: TranslateService
  ) {}

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
            this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_ALREADY_SCANNED', { model: equipmentData.model });
            this.loading = false;
            this.updateScanScenario();
            return;
          }
            this.scannedEquipments.push(equipmentData);
            this.scannedEquipmentIds.push(equipmentData.id);

            this.successMessage = this.translateService.instant('BARCODE_SCANNER.SUCCESS_ADDED', { model: equipmentData.model });
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
                  }
                  else {
                    this.handleNoActiveReservationFound();
                  }
                }
                //Only offer creation of new Reservation if no Reservation is found and no Reservation has been found
                // for other equipment
                else if (this.matchedReservations.length == 0 ) {
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
        }
        else {
          this.loading = false;
        }
      },
      error: (err) => {
        console.error('Failed to search equipment by barcode', err);
        this.loading = false;
        if (err.status === 404) {
          this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_NOT_FOUND', { value: barcode });
        } else {
          this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_UNEXPECTED');
        }
        this.updateScanScenario();
      }
    });
  }

  //Helper-Method for searchEquipment that handles the case that there were no active Reservations found for the
  // scanned pieves of Equipment
  private handleNoActiveReservationFound(): void {
    console.log('No active reservations (CREATED/PICKED_UP) found for this equipment today.');
    //TODO: Add alternative when no reservations are found here
    this.errorMessage = this.translateService.instant('TODO: Add what would be done if no reservations are found here');
  }

  //Removes the equipment, but only from the temporary scan-list - not from the database itself!
  removeEquipmentFromList(item: Equipment): void {
    this.errorMessage = '';
    this.scannedEquipments = this.scannedEquipments.filter(e => e.id !== item.id);
    this.scannedEquipmentIds = this.scannedEquipmentIds.filter(id => id !== item.id);

    this.successMessage = this.translateService.instant('BARCODE_SCANNER.SUCCESS_REMOVED', { model: item.model });
    setTimeout(() => {
      this.successMessage = '';
    }, 3000);

    //Filter out Reservations that have no more matching Equipment
    this.matchedReservations = this.matchedReservations.filter(res => {
      const hasRemainingEquipment = res.items?.some((equipment: any) => this.scannedEquipmentIds.includes(equipment.id));
      return hasRemainingEquipment;
    })

    this.updateScanScenario();
  }

  //Helper-Method that changes the current mode depending on how many corresponding reservations to the equipment
  // were found (more than 1 reservation that corresponds to equipment prevents submission of scan-check-in/out)
  //Also changes the current mode if there is equipment not corresponding to a Reservation
  updateScanScenario(): void {
    this.activeErrors = [];
    //Save existing errorMessages in activeErrors
    if (this.errorMessage){
      this.activeErrors.push(this.errorMessage);
    }
    const count = this.matchedReservations.length;

    //Part 1: Resolve mode from conflicts with Reservations
    if (count === 0) {
      this.scanScenario = 'NO_RESERVATION';
      if (this.scannedEquipments.length > 0) {
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

    //Part 2: Resolve mode from conflicts with Equipment not belonging to Reservations
    const allReservedIds = this.matchedReservations.reduce((ids: string[], res) => {
      const itemIds = res.items?.map((eq: any) => eq.id) || [];
      return [...ids, ...itemIds];
    }, []);

    const hasUnreserved = this.scannedEquipmentIds.some(id => !allReservedIds.includes(id));

    if (count > 0 && hasUnreserved) {
      this.equipmentScenario = 'EQUIPMENT_SCANNED_THAT_IS_UNRESERVED';
      this.activeErrors.push('BARCODE_SCANNER.SCENARIO_UNRESERVED_WARN');
    }
    else if (count == 1){
      const targetReservation = this.matchedReservations[0];
      const hasMissingEquipment = targetReservation.items?.some(
        (eq: any) => !this.scannedEquipmentIds.includes(eq.id)
      );

      if (hasMissingEquipment) {
        this.equipmentScenario = 'SOME_RESERVED_EQUIPMENT_SCANNED';
        this.activeErrors.push('BARCODE_SCANNER.SCENARIO_SOME_RESERVED_WARN');
      }
      else {
        this.equipmentScenario = 'ALL_RESERVED_EQUIPMENT_SCANNED';
      }
    }
    else {
      this.equipmentScenario = 'SOME_RESERVED_EQUIPMENT_SCANNED';
    }
  }

  submitExistingReservation(): void {
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

  //Helper-Method for pop-up before submission
  openConfirmationModal(dialogElement: HTMLDialogElement): void {
    if (this.scanScenario !== 'SINGLE_RESERVATION' || this.equipmentScenario !== 'ALL_RESERVED_EQUIPMENT_SCANNED') {
      return;
    }
    this.pendingCustomerName = this.matchedReservations[0].customerName || '';
    dialogElement.showModal();
  }

  //Helper-method for pop-up before submission
  confirmAndSubmit(dialogElement: HTMLDialogElement): void {
    dialogElement.close();
    this.submitExistingReservation();
  }



  //Helper-method to give RentalStatus-Enum-Values nice background coloring in HTML
  getStatusClass(status: string): string {
    switch (status) {
      case 'FREE': return 'bg-success';
      case 'RESERVED': return 'bg-warning text-dark';
      case 'RENTED': return 'bg-danger';
      case 'MAINTENANCE': return 'bg-secondary';
      default: return 'bg-light text-dark';
    }
  }

  protected readonly ReservationStatus = ReservationStatus;
}
