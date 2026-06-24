import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OverviewBarChartComponent } from './overview-bar-chart.component';

describe('OverviewBarChartComponent', () => {
  let component: OverviewBarChartComponent;
  let fixture: ComponentFixture<OverviewBarChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OverviewBarChartComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OverviewBarChartComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
