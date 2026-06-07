import {Component, OnDestroy, OnInit} from '@angular/core';
import {EquipmentService} from '../../../services/equipment.service';
import {TranslateService} from '@ngx-translate/core';
import {Equipment} from '../../../dtos/equipment';
import {RentalStatus} from '../../../dtos/rentalstatus';
import {ToastrService} from 'ngx-toastr';
import ScanbotSDK from 'scanbot-web-sdk/ui';

@Component({
  selector: 'app-barcode-scanner',
  templateUrl: './barcode-scanner.component.html',
  styleUrl: './barcode-scanner.component.scss',
  standalone: false
})
export class BarcodeScannerComponent implements OnInit, OnDestroy {
  inputBarcodeId = '';
  scannedEquipment: Equipment | null = null;
  loading = false;
  errorMessage = '';
  successMessage = '';
  activeMode: 'RENTAL' | 'MAINTENANCE' = 'RENTAL';

  isCameraOpen = false;
  private SDK: any = null;
  private scanbotScanner: any = null;

  constructor(
    private equipmentService: EquipmentService,
    public translateService: TranslateService,
    private notification: ToastrService,
  ) {
  }

  async ngOnInit() {
    try {
      this.SDK = await ScanbotSDK.initialize({
        licenseKey: '',
        enginePath: '/assets/scanbot-bin/'
      });
    } catch (error) {
      console.error('Scanbot SDK initialization failed', error);
    }
  }

  //Searches for equipment corresponding to the input barcodeId (using the getEquipmentByBarcodeId-method,
  //not the search-method of equipmentservice)
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
          this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_NOT_FOUND', {value: barcode});
        } else {
          this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_UNEXPECTED');
        }
      }
    });
  }

  //Returns the RentalStatus the equipment should have after the scan is confirmed, depending
  //on the current mode as well as the current RentalStatus
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
      this.errorMessage = this.translateService.instant('BARCODE_SCANNER.ERROR_ACTION_NOT_ALLOWED', {status: translatedStatus});
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

        this.successMessage = this.translateService.instant('BARCODE_SCANNER.SUCCESS_CHANGE', {status: finalStatusName});
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

  //TODO: Remove later with Demo
  loadDemoBarcode(barcode: string): void {
    this.inputBarcodeId = barcode;
    this.searchEquipment();
  }


  async openCamera() {
    if (!this.SDK) {
      this.notification.error('Scanner engine is not ready yet.');
      return;
    }

    const info = await this.SDK.getLicenseInfo();
    console.log('License status:', info.status, 'isValid:', info.isValid);
    if (!info.isValid) {
      this.notification.error('License invalid/expired — restart for a new trial minute.');
      this.isCameraOpen = false;
      return;
    }

    this.isCameraOpen = true;

    setTimeout(async () => {
      const configuration = {
        containerId: 'reader',
        onBarcodesDetected: (result: any) => {
          if (result && result.barcodes && result.barcodes.length > 0) {
            const scannedText = result.barcodes[0].text;

            this.onCodeResult(scannedText);
            this.closeCamera();
          }
        },
        onError: (err: any) => {
          console.error('Scanbot encountered an error', err);
          this.onScanError(err);
          this.closeCamera();
        },
        style: {
          window: {
            aspectRatio: 2.5
          }
        },
        barcodeFormats: ['CODE_128', 'QR_CODE']
      };

      try {
        this.scanbotScanner = await this.SDK.createBarcodeScanner(configuration);
      } catch (err) {
        console.error('Error starting Scanbot camera', err);
        this.isCameraOpen = false;
      }
    }, 100);
  }


  closeCamera(): void {
    this.isCameraOpen = false;
    if (this.scanbotScanner) {
      this.scanbotScanner.dispose(); // Disposing the scanner releases camera access
      this.scanbotScanner = null;
    }
  }

  ngOnDestroy() {
    this.closeCamera();
  }

  onCodeResult(resultString: string): void {
    this.notification.success("Scanned code " + resultString + " successfully.");
    this.inputBarcodeId = resultString;
    this.isCameraOpen = false;
    this.searchEquipment();
  }

  onScanError(err: any) {
    console.error('Scan error', err);
    this.notification.error("Scanner Error occurred.");
  }
}
