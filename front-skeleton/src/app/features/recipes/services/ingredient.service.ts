import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Ingredient } from '../models/ingredient.model';
import { Unit } from '../models/recipe.model';

@Injectable({ providedIn: 'root' })
export class IngredientService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/v1/ingredients`;

  search(query = ''): Observable<Ingredient[]> {
    let params = new HttpParams();
    if (query.trim()) {
      params = params.set('query', query.trim());
    }
    return this.http.get<Ingredient[]>(this.url, { params });
  }

  create(name: string, defaultUnit: Unit): Observable<Ingredient> {
    return this.http.post<Ingredient>(this.url, { name, defaultUnit });
  }
}
