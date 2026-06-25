import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisterStaffComponent } from './register-staff.component';

describe('RegisterStaffComponent', () => {
  let component: RegisterStaffComponent;
  let fixture: ComponentFixture<RegisterStaffComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterStaffComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisterStaffComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
