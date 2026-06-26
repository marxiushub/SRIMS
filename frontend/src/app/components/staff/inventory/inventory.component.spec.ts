import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {TranslateModule, TranslateService} from "@ngx-translate/core"; // Don't forget to import this at the top if it isn't there!
import {of, Subject, throwError} from "rxjs";
import {provideHttpClient} from "@angular/common/http";
import {provideHttpClientTesting} from "@angular/common/http/testing";

import {InventoryComponent} from "./inventory.component";
import {EquipmentService} from "../../../services/equipment.service";
import {RouterModule} from "@angular/router";

import {EquipmentType} from '../../../dtos/equipmenttype';
import {RentalStatus} from '../../../dtos/rentalstatus';
import {SkillLevel} from '../../../dtos/skilllevel';

import {FormsModule} from '@angular/forms';
import {NgbCollapse, NgbTypeaheadModule} from '@ng-bootstrap/ng-bootstrap';
import {ToastrModule, ToastrService} from 'ngx-toastr';


describe('InventoryComponent', () => {
  let component: InventoryComponent;
  let fixture: ComponentFixture<InventoryComponent>;
  let equipmentServiceMock: jasmine.SpyObj<EquipmentService>;
  let toastrServiceMock: jasmine.SpyObj<ToastrService>;
  let translateServiceMock: jasmine.SpyObj<TranslateService>

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
    toastrServiceMock = jasmine.createSpyObj('ToastrService', ['success', 'error', 'warning', 'info']);
    translateServiceMock = jasmine.createSpyObj('TranslateService', ['instant']);

    await TestBed.configureTestingModule({
      declarations: [InventoryComponent],
      imports: [
        RouterModule.forRoot([]),
        TranslateModule.forRoot(),
        ToastrModule.forRoot(),
        FormsModule,
        NgbCollapse,
        NgbTypeaheadModule
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: EquipmentService, useValue: equipmentServiceMock},
        {provide: ToastrService, useValue: toastrServiceMock}]
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
    expect(component.deleteError).toBeNull();
  })

  it('should cancel delete and clear delete state', () => {
    component.equipmentToDelete = testEquipment[0];
    component.deleteError = 'Some error';
    component.deleteLoading = true;

    component.cancelDelete();

    expect(component.equipmentToDelete).toBeUndefined();
    expect(component.deleteError).toBeNull();
    expect(component.deleteLoading).toBeFalse();
  });

  it('should call delete service and remove equipment from list, and show success notification', () => {
    component.equipment = [...testEquipment];
    component.equipmentToDelete = testEquipment[0];

    const translateService = TestBed.inject(TranslateService);
    spyOn(translateService, 'instant').and.returnValue('Item deleted');

    component.confirmDelete();

    expect(equipmentServiceMock.delete).toHaveBeenCalledWith(1);
    expect(component.equipment.length).toBe(1);
    expect(component.equipment[0].id).toBe(2);
    expect(component.equipmentToDelete).toBeUndefined();
    expect(component.deleteLoading).toBeFalse();
    expect(toastrServiceMock.success).toHaveBeenCalledWith('Item deleted');
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
      targetSkillLevel: SkillLevel.BEGINNER,
      start: undefined,
      end: undefined
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
      targetSkillLevel: undefined,
      start: undefined,
      end: undefined
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
    spyOn(console, 'error');

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

  describe('Pagination', () => {
    beforeEach(() => {
      // Mock enough items to span over multiple pages (limit is 5)
      const manyItems = Array.from({length: 12}).map((_, i) => ({
        id: i + 1,
        model: `Model ${i}`,
        price: 10
      })) as any[];

      component.equipment = manyItems;
      component.itemLimit = 5;
      component.currentPage = 1;
    });

    it('should calculate correct startIndex and endIndex for page 1', () => {
      expect(component.startIndex).toBe(1);
      expect(component.endIndex).toBe(5);
    });

    it('should calculate correct startIndex and endIndex for the last page', () => {
      component.goToLastPage();
      expect(component.currentPage).toBe(3);
      expect(component.startIndex).toBe(11);
      expect(component.endIndex).toBe(12); // Should cap at 12 items
    });

    it('should navigate between pages correctly', () => {
      component.nextPage();
      expect(component.currentPage).toBe(2);

      component.previousPage();
      expect(component.currentPage).toBe(1);

      // Should not go below page 1
      component.previousPage();
      expect(component.currentPage).toBe(1);
    });

    it('should go to previous page if the last item of the current page is deleted', () => {
      component.goToLastPage();

      component.equipmentToDelete = component.equipment[11];
      component.confirmDelete();

      component.equipmentToDelete = component.equipment[10];
      component.confirmDelete();

      expect(component.currentPage).toBe(2);
    });
  });

  describe('InventoryComponent - Date Validation Logic', () => {

    it('should clear endFilter if startFilter is updated to be after endFilter', () => {
      component.startFilter = '2026-05-20';
      component.endFilter = '2026-05-25';

      spyOn(component, 'searchEquipment');

      component.startFilter = '2026-05-28';
      component.onStartDateChange();

      expect(component.endFilter).toBeNull();
      expect(component.searchEquipment).toHaveBeenCalled();
    });

    it('should clear endFilter if endFilter is updated to be before startFilter', () => {
      component.startFilter = '2026-05-20';
      component.endFilter = '2026-05-15';

      spyOn(component, 'searchEquipment');

      component.onEndDateChange();

      expect(component.endFilter).toBeNull();
      expect(component.searchEquipment).toHaveBeenCalled();
    });

    it('should not clear endFilter if startFilter and endFilter are valid', () => {
      component.startFilter = '2026-05-20';
      component.endFilter = '2026-05-25';

      spyOn(component, 'searchEquipment');

      component.onStartDateChange();
      component.onEndDateChange();

      expect(component.endFilter).toBe('2026-05-25');
      expect(component.searchEquipment).toHaveBeenCalledTimes(2);
    });
  });
});
