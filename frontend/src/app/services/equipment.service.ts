import {Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {Equipment} from '../dtos/equipment';
import {EquipmentCreation} from '../dtos/equipment-creation';
import {EquipmentUpdate} from '../dtos/equipment-update';

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
   * Loads equipment by id from the backend.
   */
  getById(id: number): Observable<Equipment> {
    return this.httpClient.get<Equipment>(`${this.equipmentBaseUri}/${id}`);
  }

  /**
   * Deletes equipment by id in backend.
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

  /**
   * Updates equipment in backend.
   */
  update(id: number, equipment: EquipmentUpdate): Observable<Equipment> {
    return this.httpClient.patch<Equipment>(`${this.equipmentBaseUri}/${id}`, equipment);

  }

}
