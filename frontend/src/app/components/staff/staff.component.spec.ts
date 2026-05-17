import { ComponentFixture, TestBed } from '@angular/core/testing';
import {TranslateModule} from '@ngx-translate/core';
import {StaffComponent} from './staff.component';
import {RouterTestingModule} from "@angular/router/testing";

describe('StaffComponent', () => {
  let component: StaffComponent;
  let fixture: ComponentFixture<StaffComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StaffComponent],
      imports: [
        RouterTestingModule,
        TranslateModule.forRoot()],
    })
    .compileComponents();

    fixture = TestBed.createComponent(StaffComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
