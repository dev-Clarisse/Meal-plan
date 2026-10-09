import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InventoryItem, InventoryRequest } from '../models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private http = inject(HttpClient);
  private base = '/api/inventory';

  list(): Observable<InventoryItem[]> {
    return this.http.get<InventoryItem[]>(this.base);
  }

  create(req: InventoryRequest): Observable<InventoryItem> {
    return this.http.post<InventoryItem>(this.base, req);
  }

  update(id: string, req: InventoryRequest): Observable<InventoryItem> {
    return this.http.put<InventoryItem>(`${this.base}/${id}`, req);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}