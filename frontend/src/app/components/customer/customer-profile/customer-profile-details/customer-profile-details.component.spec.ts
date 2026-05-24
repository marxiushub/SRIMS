import {ComponentFixture, TestBed} from '@angular/core/testing';
import {CustomerProfileDetailsComponent} from './customer-profile-details.component';
import {CustomerProfileService} from '../../../../services/customer-profile.service';
import {ActivatedRoute, Router} from '@angular/router';
import {EMPTY, of, throwError} from 'rxjs';
import {SkillLevel} from '../../../../dtos/skilllevel';
import {TranslateModule} from "@ngx-translate/core";

describe('CustomerProfileDetailsComponent', () => {
  let component: CustomerProfileDetailsComponent;
  let fixture: ComponentFixture<CustomerProfileDetailsComponent>;
  let customerProfileServiceMock: jasmine.SpyObj<CustomerProfileService>;
  let routerMock: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    customerProfileServiceMock = jasmine.createSpyObj('CustomerProfileService', ['getById', 'delete']);
    customerProfileServiceMock.getById.and.returnValue(EMPTY);
    routerMock = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [CustomerProfileDetailsComponent],
      imports: [TranslateModule.forRoot()],
      providers: [
        {provide: CustomerProfileService, useValue: customerProfileServiceMock},
        {provide: Router, useValue: routerMock},
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {paramMap: {get: () => '1'}}
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CustomerProfileDetailsComponent);
    component = fixture.componentInstance;
  });

  it('should load profile on init based on route ID', () => {
    const mockProfile = {
      id: 1,
      customerId: 1,
      profileName: 'Test',
      height: 180,
      shoeSize: 42,
      skillLevel: SkillLevel.BEGINNER,
      weight: 80
    };
    customerProfileServiceMock.getById.and.returnValue(of(mockProfile));

    fixture.detectChanges(); // Triggers ngOnInit

    expect(customerProfileServiceMock.getById).toHaveBeenCalledWith(1);
    expect(component.profile).toEqual(mockProfile);
    expect(component.loading).toBeFalse();
  });

  it('should handle deletion error correctly', () => {
    const mockProfile = {
      id: 1,
      customerId: 1,
      profileName: 'Test',
      height: 180,
      shoeSize: 42,
      skillLevel: SkillLevel.BEGINNER,
      weight: 80
    };
    component.openDeleteDialog(mockProfile);

    customerProfileServiceMock.delete.and.returnValue(throwError(() => new Error('Error')));

    component.confirmDelete();

    expect(customerProfileServiceMock.delete).toHaveBeenCalledWith(1);
    expect(component.deleteError).toBe('Profile could not be deleted.');
    expect(component.deleteLoading).toBeFalse();
  });
});
