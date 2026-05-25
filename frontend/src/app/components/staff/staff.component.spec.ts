import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { By } from '@angular/platform-browser';
import { StaffComponent } from './staff.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('StaffComponent', () => {
  let component: StaffComponent;
  let fixture: ComponentFixture<StaffComponent>;
  let router: Router;
  let urlSpy: jasmine.Spy;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StaffComponent],
      imports: [
        RouterTestingModule.withRoutes([
          { path: 'staff', component: StaffComponent},
          { path: 'staff/inventory', component: StaffComponent }
        ]),
        TranslateModule.forRoot()
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StaffComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
  });

  async function navigateTo(url: string) {
    await router.navigateByUrl(url);
    fixture.detectChanges();
  }

  it('should create', async() => {
    await navigateTo('/staff');
    expect(component).toBeTruthy();
  });

  describe('isChildRouteActive', () => {
    it('should return false when the current URL is exactly "/staff"', async() => {
      await navigateTo('/staff');

      expect(component.isChildRouteActive()).toBeFalse();
    });

    it('should return false when the current URL is exactly "/staff/"', async() => {
      await navigateTo('/staff');

      spyOnProperty(router, 'url', 'get').and.returnValue('/staff/');
      expect(component.isChildRouteActive()).toBeFalse();
    });

    it('should return true when a child route is active (e.g., "/staff/inventory")', async() => {
      await navigateTo('/staff/inventory');

      expect(component.isChildRouteActive()).toBeTrue();
    });
  });

  describe('Template Rendering', () => {
    it('should display the dashboard grid when no child route is active', async() => {
      await navigateTo('/staff');

      const dashboardElement = fixture.debugElement.query(By.css('.staff-dashboard'));
      const routerOutletElement = fixture.debugElement.query(By.css('router-outlet'));

      expect(dashboardElement).toBeTruthy();
      expect(routerOutletElement).toBeTruthy();
    });

    it('should hide the dashboard grid when a child route is active', async() => {
      await navigateTo('/staff/inventory');

      const dashboardElement = fixture.debugElement.query(By.css('.staff-dashboard'));
      const routerOutletElement = fixture.debugElement.query(By.css('router-outlet'));

      expect(dashboardElement).toBeNull();
      expect(routerOutletElement).toBeTruthy();
    });
  });
});
