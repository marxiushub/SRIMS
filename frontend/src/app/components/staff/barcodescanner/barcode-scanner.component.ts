import { Component } from '@angular/core';
import { EquipmentService } from '../../../services/equipment.service';
import { TranslateService } from '@ngx-translate/core';
import { Equipment } from '../../../dtos/equipment';
import { RentalStatus } from '../../../dtos/rentalstatus';

@Component({
  selector: 'app-barcode-scanner',
  templateUrl: './barcode-scanner.component.html',
  styleUrl: './barcode-scanner.component.scss',
  standalone: false
})
export class BarcodeScannerComponent {
  inputBarcodeId = '';
  scannedEquipment: Equipment | null = null;

  loading = false;
  errorMessage = '';
  successMessage = '';

  activeMode: 'RENTAL' | 'MAINTENANCE' = 'RENTAL';

  constructor(
    private equipmentService: EquipmentService,
    public translateService: TranslateService
  ) {}

  searchEquipment(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.scannedEquipment = null;

    const barcode = this.inputBarcodeId.trim();
    if (!barcode) {
      this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_EMPTY');
      return;
    }

    this.loading = true;

    this.equipmentService.getByBarcodeId(barcode).subscribe({
      next: (data) => {
        this.scannedEquipment = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to search equipment by barcode', err);
        this.loading = false;
        if (err.status === 404) {
          this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_NOT_FOUND', { value: barcode });
        } else {
          this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_UNEXPECTED');
        }
      }
    });
  }

  getNextStatus(): RentalStatus | null {
    if (!this.scannedEquipment) {
      return null;
    }

    const currentStatus = this.scannedEquipment.status;

    if (this.activeMode === 'RENTAL') {
      if (currentStatus === RentalStatus.FREE) return RentalStatus.RENTED;
      if (currentStatus === RentalStatus.RENTED) return RentalStatus.FREE;
      return null;
    } else {
      if (currentStatus === RentalStatus.FREE) return RentalStatus.MAINTENANCE;
      if (currentStatus === RentalStatus.MAINTENANCE) return RentalStatus.FREE;
      return null;
    }
  }

  confirmScan(): void {
    const nextStatus = this.getNextStatus();

    if (!nextStatus || !this.scannedEquipment) {
      const currentTransKey = 'RENTAL_STATUS.' + this.scannedEquipment?.status;
      const translatedStatus = this.translateService.instant(currentTransKey);
      this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_ACTION_NOT_ALLOWED', { status: translatedStatus });
      return;
    }

    this.loading = true;

    const patchRequest: any = {
      type: this.scannedEquipment.equipmentType,
      status: nextStatus
    };

    this.equipmentService.update(this.scannedEquipment.id, patchRequest).subscribe({
      next: (updatedItem) => {
        const statusTransKey = 'RENTAL_STATUS.' + updatedItem.status;
        const finalStatusName = this.translateService.instant(statusTransKey);

        this.successMessage = this.translateService.instant('BARCODE_SCANNER.SUCCESS_CHANGE', { status: finalStatusName });
        this.scannedEquipment = updatedItem;
        this.inputBarcodeId = '';
        this.loading = false;

        this.scannedEquipment = null;
        this.inputBarcodeId = '';
        this.loading = false;
        setTimeout(() => {
          this.successMessage = '';
        }, 4000);
      },
      error: (err) => {
        console.error('Failed to update equipment status via barcode', err);
        this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_UNEXPECTED');
        this.loading = false;
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'FREE': return 'bg-success';
      case 'RESERVED': return 'bg-warning text-dark';
      case 'RENTED': return 'bg-danger';
      case 'MAINTENANCE': return 'bg-secondary';
      default: return 'bg-light text-dark';
    }
  }

  loadDemoBarcode(barcode: string): void {
    this.inputBarcodeId = barcode;
    this.searchEquipment();
  }
}
