import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { TranslateModule } from "@ngx-translate/core";
import { of, throwError } from "rxjs";
import { Router } from "@angular/router";

import { EquipmentCreateComponent } from "./equipment-create.component";
import { EquipmentService } from "../../../../services/equipment.service";
import { RentalStatus } from "../../../../dtos/rentalstatus";
import { EquipmentType } from "../../../../dtos/equipmenttype";
import { SkillLevel } from "../../../../dtos/skilllevel";

describe('EquipmentCreateComponent', () => {
  let component: EquipmentCreateComponent;
  let fixture: ComponentFixture<EquipmentCreateComponent>;
  let equipmentServiceMock: jasmine.SpyObj<EquipmentService>;

  const routerMock = {
    navigate: jasmine.createSpy("navigate"),
  }

  beforeEach(async () => {
    equipmentServiceMock = jasmine.createSpyObj('EquipmentService', ['create']);
    equipmentServiceMock.create.and.returnValue(of({} as any));
    routerMock.navigate.calls.reset();

    await TestBed.configureTestingModule({
      declarations: [EquipmentCreateComponent],
      imports: [FormsModule, TranslateModule.forRoot()],
      providers: [
        { provide: EquipmentService, useValue: equipmentServiceMock },
        { provide: Router, useValue: routerMock }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EquipmentCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call create service and navigate back to inventory', () => {
    component.equipment = {
      type: EquipmentType.SKI,
      model: 'Test Ski',
      status: RentalStatus.FREE,
      targetSkillLevel: SkillLevel.BEGINNER,
      price: 20,
      length: 160
    };

    component.createEquipment();

    expect(equipmentServiceMock.create).toHaveBeenCalledWith({
      type: EquipmentType.SKI,
      model: 'Test Ski',
      status: RentalStatus.FREE,
      targetSkillLevel: SkillLevel.BEGINNER,
      price: 20,
      length: 160
    });
    expect(routerMock.navigate).toHaveBeenCalledWith(['/staff/inventory']);
    expect(component.loading).toBeFalse();
    expect(component.error).toBeFalse();
  });

  it('should set error when create request fails', () => {
    equipmentServiceMock.create.and.returnValue(throwError(() => new Error('Create failed')));

    component.createEquipment();

    expect(component.error).toBeTrue();
    expect(component.loading).toBeFalse();
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });
});
