import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {Equipment} from '../dtos/equipment';
import {EquipmentCreation} from '../dtos/equipment-creation';
import {EquipmentUpdate} from '../dtos/equipment-update';
import {EquipmentSearch} from '../dtos/equipment-search';
import {EquipmentOverview} from "../dtos/equipment-overview";

@Injectable({
  providedIn: 'root'
})
export class EquipmentService {

  private equipmentBaseUri: string = this.globals.backendUri + '/equipment';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Loads all equipment from the backend.
   *
   * @return an Observable of all equipment
   */
  getAll(): Observable<Equipment[]> {
    return this.httpClient.get<Equipment[]>(this.equipmentBaseUri);
  }

  /**
   * Loads an aggregated overview of equipment counts, grouped by equipment type and rental status.
   *
   * @return an Observable of the equipment status overview
   */
  getStatusOverview(): Observable<EquipmentOverview> {
    return this.httpClient.get<EquipmentOverview>(`${this.equipmentBaseUri}/overview`);
  }

  /**
   * Loads equipment by id from the backend.
   *
   * @param id the unique id of the equipment
   * @return an Observable of the found Equipment
   */
  getById(id: number): Observable<Equipment> {
    return this.httpClient.get<Equipment>(`${this.equipmentBaseUri}/${id}`);
  }

  /**
   * Loads equipment by barcode ID from the backend.
   *
   * @param barcodeId the unique barcode string of the equipment
   * @return an Observable of the found Equipment
   */
  getByBarcodeId(barcodeId: string): Observable<Equipment> {
    return this.httpClient.get<Equipment>(`${this.equipmentBaseUri}/barcode/${barcodeId}`);
  }

  /**
   * Deletes equipment by id in backend.
   *
   * @param id the unique id of the equipment
   * @return an Observable of the deleted Equipment
   */
  delete(id: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.equipmentBaseUri}/${id}`);
  }

  /**
   * Creates new equipment in backend.
   *
   * @return an Observable of the newly created Equipment
   */
  create(equipment: EquipmentCreation): Observable<Equipment> {
    return this.httpClient.post<Equipment>(this.equipmentBaseUri, equipment);
  }

  /**
   * Updates equipment in backend.
   *
   * @return an Observable of the updated equipment
   */
  update(id: number, equipment: EquipmentUpdate): Observable<Equipment> {
    return this.httpClient.patch<Equipment>(`${this.equipmentBaseUri}/${id}`, equipment);

  }

  /**
   * Searches for pieces of equipment in backend as specified in the search parameters.
   *
   * @param search the searchParameters to be used in the search
   * @return an Observable of the found equipment that conforms to the given searchParameters
   */
  search(search: EquipmentSearch): Observable<Equipment[]> {
    let params = new HttpParams();
    if (search.model) {
      params = params.set('model', search.model);
    }
    if (search.type) {
      params = params.set('type', search.type);
    }
    if (search.status) {
      params = params.set('status', search.status);
    }
    if (search.targetSkillLevel) {
      params = params.set('targetSkillLevel', search.targetSkillLevel);
    }
    if (search.start) {
      params = params.set('start', search.start);
    }
    if(search.end) {
      params = params.set('end', search.end);
    }

    return this.httpClient.get<Equipment[]>(this.equipmentBaseUri, {params});
  }

}
