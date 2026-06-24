import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class StatisticsStateService {
  lastFilterValues: any = null;
  lastTableRows: any[] = [];
  lastMaxDaysRented: number = 1;
  lastSortDescending: boolean = true;
}
