import { TestBed } from '@angular/core/testing';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { UserService } from './user.service';
import { User, UserCreateRequest, UserUpdateRequest } from '../models/user.model';
import { environment } from '../../../environments/environment';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { jsonContentTypeInterceptor } from 'core/interceptors/json-centent-type.interceptor';


describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  const baseUrl = `${environment.apiUrl}/users`;

  // --- Mock aligné sur UserResponse (jamais de passwordHash côté client) ---
  const mockUser: User = {
    id: 'uuid-123',
    email: 'user@test.com',
    role: 'USER',
    consentGivenAt: '2026-01-01T09:00:00Z',
    createdAt: '2026-01-01T09:00:00Z',
    updatedAt: '2026-01-01T09:00:00Z',
  };

  const mockAdmin: User = {
    ...mockUser,
    id: 'uuid-admin',
    email: 'admin@test.com',
    role: 'ADMIN',
  };

  // Mot de passe valide (≥ 12 caractères, ≤ 72)
  const validPassword = 'MyS3cur3P@ssword2026';
  const newValidPassword = 'An0therS3cur3P@ss2026';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jsonContentTypeInterceptor])),
        provideHttpClientTesting(),
        UserService,
      ],
    });

    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ---------- register ----------

  it('register() should POST to /api/users with the payload', () => {
    const payload: UserCreateRequest = {
      email: 'new@test.com',
      password: validPassword,
      consent: true,
    };

    service.register(payload).subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(mockUser);
  });

  it('register() should send consent = true', () => {
    const payload: UserCreateRequest = {
      email: 'new@test.com',
      password: validPassword,
      consent: true,
    };

    service.register(payload).subscribe();

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.body.consent).toBeTrue();
    req.flush(mockUser);
  });


  it('register() should propagate 400 errors (invalid email)', () => {
    const payload: UserCreateRequest = {
      email: 'invalid-email',
      password: validPassword,
      consent: true,
    };

    let errorCaught: any = null;
    service.register(payload).subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(baseUrl);
    req.flush('Bad Request', { status: 400, statusText: 'Bad Request' });

    expect(errorCaught.status).toBe(400);
  });

  it('register() should propagate 400 errors (password too short)', () => {
    const payload: UserCreateRequest = {
      email: 'new@test.com',
      password: 'short',
      consent: true,
    };

    let errorCaught: any = null;
    service.register(payload).subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(baseUrl);
    req.flush(
      { message: 'Le mot de passe doit contenir entre 12 et 72 caractères' },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(errorCaught.status).toBe(400);
    expect(errorCaught.error.message).toContain('12 et 72');
  });

  it('register() should propagate 400 errors (consent = false)', () => {
    const payload: UserCreateRequest = {
      email: 'new@test.com',
      password: validPassword,
      consent: false,
    };

    let errorCaught: any = null;
    service.register(payload).subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(baseUrl);
    req.flush(
      { message: 'Le consentement au traitement des données est obligatoire' },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(errorCaught.status).toBe(400);
  });

  it('register() should propagate 409 errors (email already used)', () => {
    const payload: UserCreateRequest = {
      email: 'existing@test.com',
      password: validPassword,
      consent: true,
    };

    let errorCaught: any = null;
    service.register(payload).subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(baseUrl);
    req.flush('Conflict', { status: 409, statusText: 'Conflict' });

    expect(errorCaught.status).toBe(409);
  });

  // ---------- getMe ----------

  it('getMe() should GET /api/users/me', () => {
    service.getMe().subscribe(user => {
      expect(user).toEqual(mockUser);
      expect(user.email).toBe('user@test.com');
      expect(user.role).toBe('USER');
    });

    const req = httpMock.expectOne(`${baseUrl}/me`);
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });

  it('getMe() should not expose passwordHash', () => {
    service.getMe().subscribe(user => {
      expect((user as any).passwordHash).toBeUndefined();
    });

    httpMock.expectOne(`${baseUrl}/me`).flush(mockUser);
  });

  it('getMe() should propagate 401 errors', () => {
    let errorCaught: any = null;
    service.getMe().subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(`${baseUrl}/me`);
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(errorCaught.status).toBe(401);
  });

  it('getMe() should work for an ADMIN user', () => {
    service.getMe().subscribe(user => {
      expect(user.role).toBe('ADMIN');
      expect(user.email).toBe('admin@test.com');
    });

    httpMock.expectOne(`${baseUrl}/me`).flush(mockAdmin);
  });

  // ---------- updateMe ----------

  it('updateMe() should PUT to /api/users/me (email only)', () => {
    const payload: UserUpdateRequest = {
      currentPassword: validPassword,
      email: 'new@test.com',
    };
    const updated: User = { ...mockUser, email: 'new@test.com' };

    service.updateMe(payload).subscribe(user => {
      expect(user.email).toBe('new@test.com');
    });

    const req = httpMock.expectOne(`${baseUrl}/me`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);
    expect(req.request.body.currentPassword).toBe(validPassword);
    req.flush(updated);
  });

  it('updateMe() should PUT to /api/users/me (password change)', () => {
    const payload: UserUpdateRequest = {
      currentPassword: validPassword,
      newPassword: newValidPassword,
    };

    service.updateMe(payload).subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne(`${baseUrl}/me`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.currentPassword).toBe(validPassword);
    expect(req.request.body.newPassword).toBe(newValidPassword);
    req.flush(mockUser);
  });

  it('updateMe() should PUT to /api/users/me (email + password)', () => {
    const payload: UserUpdateRequest = {
      currentPassword: validPassword,
      email: 'new@test.com',
      newPassword: newValidPassword,
    };

    service.updateMe(payload).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/me`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);
    req.flush(mockUser);
  });

  it('updateMe() should propagate 400 errors (wrong currentPassword)', () => {
    const payload: UserUpdateRequest = {
      currentPassword: 'wrongPassword',
      newPassword: newValidPassword,
    };

    let errorCaught: any = null;
    service.updateMe(payload).subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(`${baseUrl}/me`);
    req.flush(
      { message: 'Mot de passe actuel incorrect' },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(errorCaught.status).toBe(400);
    expect(errorCaught.error.message).toContain('incorrect');
  });

  it('updateMe() should propagate 400 errors (new password too short)', () => {
    const payload: UserUpdateRequest = {
      currentPassword: validPassword,
      newPassword: 'short',
    };

    let errorCaught: any = null;
    service.updateMe(payload).subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(`${baseUrl}/me`);
    req.flush(
      { message: 'Le mot de passe doit contenir entre 12 et 72 caractères' },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(errorCaught.status).toBe(400);
  });

  it('updateMe() should propagate 400 errors (invalid email format)', () => {
    const payload: UserUpdateRequest = {
      currentPassword: validPassword,
      email: 'not-an-email',
    };

    let errorCaught: any = null;
    service.updateMe(payload).subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(`${baseUrl}/me`);
    req.flush('Bad Request', { status: 400, statusText: 'Bad Request' });

    expect(errorCaught.status).toBe(400);
  });

  it('updateMe() should propagate 401 errors (no token)', () => {
    const payload: UserUpdateRequest = {
      currentPassword: validPassword,
      email: 'new@test.com',
    };

    let errorCaught: any = null;
    service.updateMe(payload).subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(`${baseUrl}/me`);
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(errorCaught.status).toBe(401);
  });

  // ---------- deleteMe ----------

  it('deleteMe() should DELETE /api/users/me', () => {
    let completed = false;

    service.deleteMe().subscribe(() => (completed = true));

    const req = httpMock.expectOne(`${baseUrl}/me`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    expect(completed).toBeTrue();
  });

  it('deleteMe() should propagate 403 errors', () => {
    let errorCaught: any = null;

    service.deleteMe().subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(`${baseUrl}/me`);
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });

    expect(errorCaught.status).toBe(403);
  });

  it('deleteMe() should propagate 401 errors', () => {
    let errorCaught: any = null;

    service.deleteMe().subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(`${baseUrl}/me`);
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(errorCaught.status).toBe(401);
  });

  // ---------- exportMe ----------

  it('exportMe() should GET /api/users/me/export', () => {
    service.exportMe().subscribe(user => {
      expect(user).toEqual(mockUser);
      expect(user.consentGivenAt).toBe('2026-01-01T09:00:00Z');
    });

    const req = httpMock.expectOne(`${baseUrl}/me/export`);
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });

  it('exportMe() should propagate 401 errors', () => {
    let errorCaught: any = null;

    service.exportMe().subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(`${baseUrl}/me/export`);
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(errorCaught.status).toBe(401);
  });

  // ---------- vérification globale des endpoints ----------

  it('should use the correct endpoint and method for every operation', () => {
    // register
    service.register({ email: 'a@b.com', password: validPassword, consent: true }).subscribe();
    const req1 = httpMock.expectOne(baseUrl);
    expect(req1.request.method).toBe('POST');
    req1.flush(mockUser);

    // getMe
    service.getMe().subscribe();
    const req2 = httpMock.expectOne(`${baseUrl}/me`);
    expect(req2.request.method).toBe('GET');
    req2.flush(mockUser);

    // updateMe
    service.updateMe({ currentPassword: validPassword, email: 'new@test.com' }).subscribe();
    const req3 = httpMock.expectOne(`${baseUrl}/me`);
    expect(req3.request.method).toBe('PUT');
    req3.flush(mockUser);

    // deleteMe
    service.deleteMe().subscribe();
    const req4 = httpMock.expectOne(`${baseUrl}/me`);
    expect(req4.request.method).toBe('DELETE');
    req4.flush(null);

    // exportMe
    service.exportMe().subscribe();
    const req5 = httpMock.expectOne(`${baseUrl}/me/export`);
    expect(req5.request.method).toBe('GET');
    req5.flush(mockUser);
  });

  it('register() should send the Content-Type header', () => {
  const payload: UserCreateRequest = {
    email: 'new@test.com',
    password: validPassword,
    consent: true,
  };

  service.register(payload).subscribe();

  const req = httpMock.expectOne(baseUrl);
  expect(req.request.headers.get('Content-Type')).toContain('application/json');
  req.flush(mockUser);
});

});