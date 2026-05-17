import { ComponentFixture, TestBed } from '@angular/core/testing';
import {TranslateModule} from "@ngx-translate/core";
import {of} from "rxjs";
import { provideHttpClient } from "@angular/common/http";
import { provideHttpClientTesting } from "@angular/common/http/testing";

import {InventoryComponent} from "./inventory.component";
import {EquipmentService} from "../../../services/equipment.service";
import {RouterModule} from "@angular/router";


describe('InventoryComponent', () => {
  let component: InventoryComponent;
  let fixture: ComponentFixture<InventoryComponent>;

  //Mock for Frontend-Tests so we don't send real Backend-Requests
  const equipmentServiceMock = {
    getAll: () => of([])
  };

  beforeEach(async () => {
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
});
