import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from "@ngx-translate/core";
import { of, throwError } from "rxjs";
import { provideHttpClient } from "@angular/common/http";
import { provideHttpClientTesting } from "@angular/common/http/testing";

import {InventoryComponent} from "./inventory.component";
import {EquipmentService} from "../../../services/equipment.service";
import {RouterModule} from "@angular/router";


describe('InventoryComponent', () => {
  let component: InventoryComponent;
  let fixture: ComponentFixture<InventoryComponent>;
  let equipmentServiceMock: jasmine.SpyObj<EquipmentService>;

  const testEquipment : any[] = [
    {
      id: 1,
      model: 'Test Ski',
      equipmentType: 'SKI',
      status: 'FREE',
      targetSkillLevel: 'BEGINNER',
      price: 20
    },
    {
      id: 2,
      model: 'Test Helmet',
      equipmentType: 'HELMET',
      status: 'FREE',
      targetSkillLevel: 'BEGINNER',
      price: 10
    }
  ]

  beforeEach(async () => {
    equipmentServiceMock = jasmine.createSpyObj('EqipmentService', ['getAll', 'delete']);
    equipmentServiceMock.getAll.and.returnValue(of([]));
    equipmentServiceMock.delete.and.returnValue(of(void 0));


    await TestBed.configureTestingModule({
      declarations: [InventoryComponent],
      imports: [
        RouterModule.forRoot([]),
        TranslateModule.forRoot()],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: EquipmentService, useValue: equipmentServiceMock},]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InventoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should open delete dialog for selected equipment', () => {
    component.openDeleteDialog(testEquipment[0]);

    expect(component.equipmentToDelete).toEqual(testEquipment[0]);
    expect(component.deleteError).toBeUndefined();
  })

  it('should cancel delete and clear delete state', () => {
    component.equipmentToDelete = testEquipment[0];
    component.deleteError = 'Some error';
    component.deleteLoading = true;

    component.cancelDelete();

    expect(component.equipmentToDelete).toBeUndefined();
    expect(component.deleteError).toBeUndefined();
    expect(component.deleteLoading).toBeFalse();
  });

  it('should call delete service and remove equipment from list', () => {
    component.equipment = [...testEquipment];
    component.equipmentToDelete = testEquipment[0];

    component.confirmDelete();

    expect(equipmentServiceMock.delete).toHaveBeenCalledWith(1);
    expect(component.equipment.length).toBe(1);
    expect(component.equipment[0].id).toBe(2);
    expect(component.equipmentToDelete).toBeUndefined();
    expect(component.deleteLoading).toBeFalse();
  });

});
