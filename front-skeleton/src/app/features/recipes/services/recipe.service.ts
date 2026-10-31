import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Page, Recipe, RecipeRequest, RecipeDraft } from '../models/recipe.model';

@Injectable({ providedIn: 'root' })
export class RecipeService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/v1/recipes`;

  search(title: string, page: number, size: number): Observable<Page<Recipe>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (title.trim()) {
      params = params.set('title', title.trim());
    }
    return this.http.get<Page<Recipe>>(this.url, { params });
  }

  findById(id: number): Observable<Recipe> {
    return this.http.get<Recipe>(`${this.url}/${id}`);
  }

  create(request: RecipeRequest): Observable<Recipe> {
    return this.http.post<Recipe>(this.url, request);
  }

  update(id: number, request: RecipeRequest): Observable<Recipe> {
    return this.http.put<Recipe>(`${this.url}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  generate(prompt: string, servings?: number): Observable<RecipeDraft> {
    return this.http.post<RecipeDraft>(`${this.url}/generate`, { prompt, servings });
  }
}
