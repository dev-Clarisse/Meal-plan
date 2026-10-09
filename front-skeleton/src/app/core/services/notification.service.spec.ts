import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { NotificationService } from './notification.service';
import { Notification } from '../models/notification.model';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { jsonContentTypeInterceptor } from 'core/interceptors/json-centent-type.interceptor';

describe('NotificationService', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;

  const baseUrl = '/api/notifications';

  const mockNotifs: Notification[] = [
    {
      id: 'n1',
      inventoryId: 'i1',
      inventoryName: 'Lait',
      type: 'EXPIRING_SOON',
      message: 'Le produit Lait expire bientôt',
      read: false,
      createdAt: '2026-10-09T07:00:00Z',
    },
    {
      id: 'n2',
      inventoryId: 'i2',
      inventoryName: 'Fromage',
      type: 'EXPIRED',
      message: 'Le produit Fromage a expiré',
      read: true,
      createdAt: '2026-10-08T07:00:00Z',
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jsonContentTypeInterceptor])),
        provideHttpClientTesting(),
        NotificationService,
      ],
    });

    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ---------- list ----------

  it('list() should GET /api/notifications', () => {
    service.list().subscribe(notifs => {
      expect(notifs).toEqual(mockNotifs);
      expect(notifs.length).toBe(2);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockNotifs);
  });

  it('list() should return an empty array when there are no notifications', () => {
    service.list().subscribe(notifs => {
      expect(notifs).toEqual([]);
    });

    const req = httpMock.expectOne(baseUrl);
    req.flush([]);
  });

  // ---------- unreadCount ----------

  it('unreadCount() should GET /unread-count and extract count', () => {
    service.unreadCount().subscribe(count => {
      expect(count).toBe(3);
    });

    const req = httpMock.expectOne(`${baseUrl}/unread-count`);
    expect(req.request.method).toBe('GET');
    req.flush({ count: 3 });
  });

  it('unreadCount() should return 0 when no unread notifications', () => {
    service.unreadCount().subscribe(count => {
      expect(count).toBe(0);
    });

    const req = httpMock.expectOne(`${baseUrl}/unread-count`);
    req.flush({ count: 0 });
  });

  it('unreadCount() should map the response (not return the wrapper object)', () => {
    let result: any = null;

    service.unreadCount().subscribe(c => (result = c));

    httpMock.expectOne(`${baseUrl}/unread-count`).flush({ count: 7 });

    expect(result).toBe(7);
    expect(typeof result).toBe('number');
  });

  // ---------- markAsRead ----------

  it('markAsRead() should POST to /{id}/read', () => {
    const id = 'n1';
    let completed = false;

    service.markAsRead(id).subscribe(() => (completed = true));

    const req = httpMock.expectOne(`${baseUrl}/${id}/read`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});  // corps vide
    req.flush(null);

    expect(completed).toBeTrue();
  });

  it('markAsRead() should propagate errors', () => {
    let errorCaught: any = null;

    service.markAsRead('n1').subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(`${baseUrl}/n1/read`);
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });

    expect(errorCaught.status).toBe(403);
  });

  // ---------- markAllAsRead ----------

  it('markAllAsRead() should POST to /read-all', () => {
    let completed = false;

    service.markAllAsRead().subscribe(() => (completed = true));

    const req = httpMock.expectOne(`${baseUrl}/read-all`);
    expect(req.request.method).toBe('POST');
    req.flush(null);

    expect(completed).toBeTrue();
  });

  // ---------- vérif URLs ----------

  it('should use the correct endpoint for every method', () => {
    service.list().subscribe();
    service.unreadCount().subscribe();
    service.markAsRead('abc').subscribe();
    service.markAllAsRead().subscribe();

    const listReq = httpMock.expectOne(baseUrl);
    expect(listReq.request.method).toBe('GET');
    listReq.flush([]);

    const countReq = httpMock.expectOne(`${baseUrl}/unread-count`);
    expect(countReq.request.method).toBe('GET');
    countReq.flush({ count: 0 });

    const readReq = httpMock.expectOne(`${baseUrl}/abc/read`);
    expect(readReq.request.method).toBe('POST');
    readReq.flush(null);

    const readAllReq = httpMock.expectOne(`${baseUrl}/read-all`);
    expect(readAllReq.request.method).toBe('POST');
    readAllReq.flush(null);
  });
});