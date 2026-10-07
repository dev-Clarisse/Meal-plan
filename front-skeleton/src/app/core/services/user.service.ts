import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, UserCreateRequest, UserUpdateRequest } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/users`;

  /** Inscription (public) */
  register(request: UserCreateRequest): Observable<User> {
    return this.http.post<User>(this.url, request);
  }

  /** Droit d'accès */
  getMe(): Observable<User> {
    return this.http.get<User>(`${this.url}/me`);
  }

  /** Droit de rectification */
  updateMe(request: UserUpdateRequest): Observable<User> {
    return this.http.put<User>(`${this.url}/me`, request);
  }

  /** Droit à l'effacement */
  deleteMe(): Observable<void> {
    return this.http.delete<void>(`${this.url}/me`);
  }

  /** Droit à la portabilité (JSON) */
  exportMe(): Observable<User> {
    return this.http.get<User>(`${this.url}/me/export`);
  }
}
