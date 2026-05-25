import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { EquipmentViewComponent } from './equipment-view.component';
import { EquipmentService } from '../../../../services/equipment.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';

registerLocaleData(localeDe, 'de');

//AI-assisted: Code generated with Google Gemini and adapted
describe('EquipmentViewComponent', () => {
  let component: EquipmentViewComponent;
  let fixture: ComponentFixture<EquipmentViewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [EquipmentViewComponent],
      imports: [
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        EquipmentService,
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => '1'
              }
            }
          }
        }
        // ----------------------------------------------------
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EquipmentViewComponent);
    component = fixture.componentInstance;

    const equipmentService = TestBed.inject(EquipmentService);
    spyOn(equipmentService, 'getById').and.returnValue(of({
      id: 1,
      barcodeId: '12345',
      price: 25.0,
      model: 'Test Ski',
      status: 'FREE',
      targetSkillLevel: 'BEGINNER',
      equipmentType: 'SKI',
      length: 170
    } as any));

    fixture.detectChanges();
  });

  it('should create the view component', () => {
    expect(component).toBeTruthy();
  });
});
