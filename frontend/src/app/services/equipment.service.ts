import {Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {Globals} from '../global/globals';
import {Equipment} from '../dtos/equipment';

@Injectable({
  providedIn: 'root'
})
export class EquipmentService {

  private equipmentBaseUri: string = this.globals.backendUri + '/equipment';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Loads all equipment from the backend
   */
  getAll(): Observable<Equipment[]> {
    return this.httpClient.get<Equipment[]>(this.equipmentBaseUri);
  }

}
