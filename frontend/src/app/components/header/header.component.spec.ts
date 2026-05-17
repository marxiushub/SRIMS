import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {HeaderComponent} from './header.component';
import {AppComponent} from "../../app.component";
import {TranslateModule} from '@ngx-translate/core';
import {RouterTestingModule} from '@angular/router/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;

  const appComponentMock = {
    title: 'SE PR Group Phase'
  }

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
    declarations: [HeaderComponent],
    imports: [
      RouterTestingModule,
      TranslateModule.forRoot()],
    providers: [
      provideHttpClient(withInterceptorsFromDi()),
      provideHttpClientTesting(),
      {provide: AppComponent, useValue: appComponentMock}]
})
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
