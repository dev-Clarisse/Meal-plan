import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, TokenResponse } from '../models/auth.model';

const TOKEN_KEY = 'mp_token';
const EXPIRES_KEY = 'mp_token_expires_at';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private loggedIn$ = new BehaviorSubject<boolean>(this.isAuthenticated());

  readonly isLoggedIn$ = this.loggedIn$.asObservable();

  login(credentials: LoginRequest): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(
        tap((res) => {
          // sessionStorage : le token disparaît à la fermeture de l'onglet
          sessionStorage.setItem(TOKEN_KEY, res.accessToken);
          sessionStorage.setItem(EXPIRES_KEY, String(Date.now() + res.expiresIn * 1000));
          this.loggedIn$.next(true);
        })
      );
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(EXPIRES_KEY);
    this.loggedIn$.next(false);
  }

  getToken(): string | null {
    return this.isAuthenticated() ? sessionStorage.getItem(TOKEN_KEY) : null;
  }

  // Le JWT dure 15 min par défaut et il n'y a pas de refresh token :
  // on considère l'utilisateur déconnecté une fois l'expiration passée.
  isAuthenticated(): boolean {
    const token = sessionStorage.getItem(TOKEN_KEY);
    const expiresAt = Number(sessionStorage.getItem(EXPIRES_KEY));
    if (!token || !expiresAt || Date.now() >= expiresAt) {
      sessionStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem(EXPIRES_KEY);
      return false;
    }
    return true;
  }
}
