import {ComponentFixture, TestBed} from '@angular/core/testing';
import {CustomerComponent} from './customer.component';
import {RouterModule} from "@angular/router";

describe('CustomerComponent', () => {
  let component: CustomerComponent;
  let fixture: ComponentFixture<CustomerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CustomerComponent],
      imports: [RouterModule.forRoot([])]
    })
      .compileComponents();

    fixture = TestBed.createComponent(CustomerComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
