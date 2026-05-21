import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {TranslateModule} from "@ngx-translate/core";
import {of, Subject, throwError} from "rxjs";
import {provideHttpClient} from "@angular/common/http";
import {provideHttpClientTesting} from "@angular/common/http/testing";

import {InventoryComponent} from "./inventory.component";
import {EquipmentService} from "../../../services/equipment.service";
import {RouterModule} from "@angular/router";

import {EquipmentType} from '../../../dtos/equipmenttype';
import {RentalStatus} from '../../../dtos/rentalstatus';
import {SkillLevel} from '../../../dtos/skilllevel';

describe('InventoryComponent', () => {
  let component: InventoryComponent;
  let fixture: ComponentFixture<InventoryComponent>;
  let equipmentServiceMock: jasmine.SpyObj<EquipmentService>;

  const testEquipment: any[] = [
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
    equipmentServiceMock = jasmine.createSpyObj('EquipmentService', ['getAll', 'delete', 'search']);
    equipmentServiceMock.getAll.and.returnValue(of([]));
    equipmentServiceMock.delete.and.returnValue(of(void 0));
    equipmentServiceMock.search.and.returnValue(of([]));


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

  it('should call search with trimmed model and selected filters', () => {
    component.modelFilter = 'Test Ski';
    component.typeFilter = EquipmentType.SKI as any;
    component.statusFilter = RentalStatus.FREE as any;
    component.skillFilter = SkillLevel.BEGINNER as any;

    equipmentServiceMock.search.and.returnValue(of([...testEquipment]));

    component.searchEquipment();

    expect(equipmentServiceMock.search).toHaveBeenCalledWith({
      model: 'Test Ski',
      type: EquipmentType.SKI,
      status: RentalStatus.FREE,
      targetSkillLevel: SkillLevel.BEGINNER
    });
    expect(component.loading).toBeFalse();
  });

  it('should pass undefined for empty filters', () => {
    component.modelFilter = '   ';
    component.typeFilter = null;
    component.statusFilter = null;
    component.skillFilter = null;

    equipmentServiceMock.search.and.returnValue(of([]));

    component.searchEquipment();

    expect(equipmentServiceMock.search).toHaveBeenCalledWith({
      model: undefined,
      type: undefined,
      status: undefined,
      targetSkillLevel: undefined
    });
  });

  it('should clear filters, reset sort, and reload equipment', () => {
    component.modelFilter = 'foo';
    component.typeFilter = EquipmentType.SKI as any;
    component.statusFilter = RentalStatus.FREE as any;
    component.skillFilter = SkillLevel.BEGINNER as any;
    component.priceSortDirection = 'desc';

    const loaded = [
      {...testEquipment[0], id: 1, price: 20},
      {...testEquipment[1], id: 2, price: 10}
    ];
    equipmentServiceMock.getAll.and.returnValue(of(loaded as any));

    component.clearFilters();

    expect(component.modelFilter).toBe('');
    expect(component.typeFilter).toBeNull();
    expect(component.statusFilter).toBeNull();
    expect(component.skillFilter).toBeNull();
    expect(component.priceSortDirection).toBe('asc');
    expect(equipmentServiceMock.getAll).toHaveBeenCalled();
    expect(component.equipment.map(e => e.id)).toEqual([2, 1]); // asc after reload
  });

  it('should set loading false when search fails', () => {
    component.loading = false;
    equipmentServiceMock.search.and.returnValue(
      throwError(() => new Error('search failed'))
    );

    component.searchEquipment();

    expect(component.loading).toBeFalse();
  });

  it('should return case-insensitive model suggestions with debounce and max 10 results', fakeAsync(() => {
    component.modelOptions = [
      'Ski Alpha',
      'ski Beta',
      'Helmet Pro',
      'Ski Carbon',
      'Ski Touring',
      'Ski Race',
      'Ski Junior',
      'Ski Women',
      'Ski Men',
      'Ski Kids',
      'Ski Extra',
      'Pole Basic'
    ];

    const text$ = new Subject<string>();
    let suggestions: readonly string[] = [];

    component.searchModel(text$).subscribe(result => {
      suggestions = result;
    });

    text$.next('SKI');
    tick(150);

    expect(suggestions.length).toBe(10);
    expect(suggestions.every(s => s.toLowerCase().includes('ski'))).toBeTrue();
  }));

  it('should build unique, sorted model options when equipment is loaded', () => {
    const loaded = [
      {...testEquipment[0], id: 1, model: 'Ski Z'},
      {...testEquipment[1], id: 2, model: 'Ski A'},
      {...testEquipment[0], id: 3, model: 'Ski A'}, // duplicate
      {...testEquipment[1], id: 4, model: 'Helmet Pro'}
    ];

    equipmentServiceMock.getAll.and.returnValue(of(loaded as any));

    component.loadEquipment();

    expect(component.modelOptions).toEqual(['Helmet Pro', 'Ski A', 'Ski Z']);
  });
});
