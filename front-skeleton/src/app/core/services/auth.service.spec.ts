import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { TokenResponse } from '../models/auth.model';

// Mêmes clés que dans auth.service.ts
const TOKEN_KEY = 'mp_token';
const EXPIRES_KEY = 'mp_token_expires_at';

describe('AuthService', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear(); // chaque test repart d'un navigateur "vide"
    TestBed.configureTestingModule({
      // HttpClient "de test" : aucune vraie requête réseau n'est envoyée
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // échoue si une requête inattendue est restée en suspens
    sessionStorage.clear();
  });

  it('login() envoie POST /api/auth/login, stocke le token et son expiration', () => {
    const service = TestBed.inject(AuthService);
    const emitted: boolean[] = [];
    service.isLoggedIn$.subscribe((value) => emitted.push(value));
    const response: TokenResponse = { accessToken: 'jwt-factice', tokenType: 'Bearer', expiresIn: 900 };
    const before = Date.now();

    service.login({ email: 'clarisse@mail.com', password: 'motdepasse-12345' }).subscribe();

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'clarisse@mail.com', password: 'motdepasse-12345' });
    req.flush(response); // simule la réponse du back

    expect(sessionStorage.getItem(TOKEN_KEY)).toBe('jwt-factice');
    expect(Number(sessionStorage.getItem(EXPIRES_KEY))).toBeGreaterThanOrEqual(before + 900 * 1000);
    expect(emitted).toEqual([false, true]);
    expect(service.isAuthenticated()).toBeTrue();
    expect(service.getToken()).toBe('jwt-factice');
  });

  it('login() en échec (401) ne stocke rien', () => {
    const service = TestBed.inject(AuthService);
    let status = 0;

    service.login({ email: 'clarisse@mail.com', password: 'mauvais' }).subscribe({
      error: (err) => (status = err.status),
    });
    httpMock
      .expectOne('/api/auth/login')
      .flush({ message: 'Identifiants invalides' }, { status: 401, statusText: 'Unauthorized' });

    expect(status).toBe(401);
    expect(sessionStorage.getItem(TOKEN_KEY)).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('isAuthenticated() est faux sans token', () => {
    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.getToken()).toBeNull();
  });

  it('isAuthenticated() est vrai avec un token non expiré', () => {
    sessionStorage.setItem(TOKEN_KEY, 'jwt-factice');
    sessionStorage.setItem(EXPIRES_KEY, String(Date.now() + 60_000));
    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.getToken()).toBe('jwt-factice');
  });

  it('un token expiré est refusé et nettoyé du stockage', () => {
    sessionStorage.setItem(TOKEN_KEY, 'jwt-expire');
    sessionStorage.setItem(EXPIRES_KEY, String(Date.now() - 1_000));
    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.getToken()).toBeNull();
    expect(sessionStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it('logout() efface le token et notifie isLoggedIn$', () => {
    sessionStorage.setItem(TOKEN_KEY, 'jwt-factice');
    sessionStorage.setItem(EXPIRES_KEY, String(Date.now() + 60_000));
    const service = TestBed.inject(AuthService);
    const emitted: boolean[] = [];
    service.isLoggedIn$.subscribe((value) => emitted.push(value));

    service.logout();

    expect(sessionStorage.getItem(TOKEN_KEY)).toBeNull();
    expect(sessionStorage.getItem(EXPIRES_KEY)).toBeNull();
    expect(emitted).toEqual([true, false]);
  });
});