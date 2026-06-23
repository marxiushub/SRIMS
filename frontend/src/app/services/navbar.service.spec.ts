import { TestBed } from '@angular/core/testing';
import { NavbarService } from './navbar.service';

describe('NavbarService', () => {
  let service: NavbarService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NavbarService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should emit on closeNavbar$ when close() is called', (done) => {
    service.closeNavbar$.subscribe(() => {
      expect(true).toBeTrue();
      done();
    });

    service.close();
  });

  it('should emit multiple times when close() is called multiple times', (done) => {
    let count = 0;

    service.closeNavbar$.subscribe(() => {
      count++;
      if (count === 3) {
        expect(count).toBe(3);
        done();
      }
    });

    service.close();
    service.close();
    service.close();
  });
});
