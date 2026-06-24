import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OverviewTilesComponent } from './overview-tiles.component';

describe('OverviewTilesComponent', () => {
  let component: OverviewTilesComponent;
  let fixture: ComponentFixture<OverviewTilesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OverviewTilesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OverviewTilesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
