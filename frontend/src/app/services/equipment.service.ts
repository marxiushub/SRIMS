import {Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {Equipment} from '../dtos/equipment';
import {EquipmentCreation} from '../dtos/equipment-creation';

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

  /**
   * Deletes equipment by id in backend.
   *
   * Current expected endpoint: DELETE /api/v1/equipment/{id}
   * TODO: Change to correct endpoint.
   */
  delete(id: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.equipmentBaseUri}/${id}`);
  }

  /**
   * Creates new equipment in backend
   */
  create(equipment: EquipmentCreation): Observable<Equipment> {
    return this.httpClient.post<Equipment>(this.equipmentBaseUri, equipment);
  }

}
