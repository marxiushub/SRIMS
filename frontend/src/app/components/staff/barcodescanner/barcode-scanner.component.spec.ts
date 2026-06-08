import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {BarcodeScannerComponent} from './barcode-scanner.component';
import {FormsModule} from '@angular/forms';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';
import {EquipmentService} from '../../../services/equipment.service';

import {RentalStatus} from '../../../dtos/rentalstatus';
import {Equipment} from '../../../dtos/equipment';
import {ToastrService} from 'ngx-toastr';
import ScanbotSDK from 'scanbot-web-sdk/ui';

describe('BarcodeScannerComponent', () => {
  let component: BarcodeScannerComponent;
  let fixture: ComponentFixture<BarcodeScannerComponent>;
  let equipmentServiceMock: any;
  let translateService: TranslateService;

  const mockEquipment: Equipment = {
    id: 1,
    barcodeId: '92f98472-a7cc-4953-8192-ae6ab015cf22',
    model: 'K2 Power Composite',
    equipmentType: 'POLE' as any,
    status: RentalStatus.FREE,
    price: 15,
    targetSkillLevel: 'BEGINNER' as any
  };

  beforeEach(async () => {
    equipmentServiceMock = {
      getByBarcodeId: jasmine.createSpy('getByBarcodeId').and.returnValue(of(mockEquipment)),
      update: jasmine.createSpy('update').and.returnValue(of({...mockEquipment, status: RentalStatus.RENTED}))
    };
    const toastrMock = {
      success: jasmine.createSpy('success'),
      error: jasmine.createSpy('error')
    };

    await TestBed.configureTestingModule({
      declarations: [BarcodeScannerComponent],
      imports: [
        FormsModule,
        TranslateModule.forRoot()
      ],
      providers: [
        {provide: EquipmentService, useValue: equipmentServiceMock},
        {provide: ToastrService, useValue: toastrMock}
      ]
    }).compileComponents();

    spyOn(ScanbotSDK, 'initialize').and.returnValue(Promise.resolve({
      getLicenseInfo: () => Promise.resolve({status: 'OK', isValid: true}),
      createBarcodeScanner: () => Promise.resolve({
        dispose: () => {
        }
      })
    } as any));

    fixture = TestBed.createComponent(BarcodeScannerComponent);
    component = fixture.componentInstance;
    translateService = TestBed.inject(TranslateService);
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.activeMode).toBe('RENTAL');
    expect(component.inputBarcodeId).toBe('');
    expect(component.scannedEquipment).toBeNull();
  });

  it('should show error message if searching with empty barcode', () => {
    component.inputBarcodeId = '   ';
    component.searchEquipment();

    expect(component.errorMessage).toBeTruthy();
    expect(equipmentServiceMock.getByBarcodeId).not.toHaveBeenCalled();
  });

  it('should successfully search and find equipment', () => {
    component.inputBarcodeId = '92f98472-a7cc-4953-8192-ae6ab015cf22';
    component.searchEquipment();

    expect(component.loading).toBeFalse();
    expect(equipmentServiceMock.getByBarcodeId).toHaveBeenCalledWith('92f98472-a7cc-4953-8192-ae6ab015cf22');
    expect(component.scannedEquipment).toEqual(mockEquipment);
    expect(component.errorMessage).toBe('');
  });

  it('should handle 404 error when equipment is not found', () => {
    equipmentServiceMock.getByBarcodeId.and.returnValue(throwError(() => ({status: 404})));
    component.inputBarcodeId = 'unknown-id';
    component.searchEquipment();

    expect(component.scannedEquipment).toBeNull();
    expect(component.errorMessage).toBeTruthy();
  });

  it('should calculate correct next status based on active mode', () => {
    component.scannedEquipment = {...mockEquipment, status: RentalStatus.FREE};

    component.activeMode = 'RENTAL';
    expect(component.getNextStatus()).toBe(RentalStatus.RENTED);

    component.scannedEquipment!.status = RentalStatus.RENTED;
    expect(component.getNextStatus()).toBe(RentalStatus.FREE);

    component.activeMode = 'MAINTENANCE';
    component.scannedEquipment!.status = RentalStatus.FREE;
    expect(component.getNextStatus()).toBe(RentalStatus.MAINTENANCE);
  });

  it('should successfully update status and close the preview window', fakeAsync(() => {
    component.scannedEquipment = {...mockEquipment, status: RentalStatus.FREE};
    component.activeMode = 'RENTAL';

    component.confirmScan();

    expect(equipmentServiceMock.update).toHaveBeenCalledWith(mockEquipment.id, {
      type: 'POLE',
      status: RentalStatus.RENTED
    });

    expect(component.scannedEquipment).toBeNull();
    expect(component.inputBarcodeId).toBe('');
    expect(component.successMessage).toBeTruthy();

    tick(4000);
    expect(component.successMessage).toBe('');
  }));

  it('should load demo barcode when clicking a demo item', () => {
    component.loadDemoBarcode('demo-id');

    expect(component.inputBarcodeId).toBe('demo-id');
    expect(equipmentServiceMock.getByBarcodeId).toHaveBeenCalledWith('demo-id');
  });
});
