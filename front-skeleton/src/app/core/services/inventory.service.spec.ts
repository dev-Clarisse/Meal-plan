import { TestBed } from '@angular/core/testing';
import { InventoryService } from './inventory.service';
import { InventoryItem, InventoryRequest } from '../models/inventory.model';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { jsonContentTypeInterceptor } from 'core/interceptors/json-centent-type.interceptor';

describe('InventoryService', () => {
  let service: InventoryService;
  let httpMock: HttpTestingController;

  const baseUrl = '/api/inventory';

  const mockItems: InventoryItem[] = [
    {
      id: '1',
      name: 'Lait',
      quantity: 1.5,
      unit: 'LITER',
      expiryDate: '2026-10-20',
      status: 'OK',
      createdAt: '2026-10-01T10:00:00Z',
      updatedAt: '2026-10-01T10:00:00Z',
    },
    {
      id: '2',
      name: 'Yaourt',
      quantity: 2,
      unit: 'PIECE',
      expiryDate: '2026-10-11',
      status: 'EXPIRING_SOON',
      createdAt: '2026-10-01T10:00:00Z',
      updatedAt: '2026-10-01T10:00:00Z',
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jsonContentTypeInterceptor])),
        provideHttpClientTesting(),
        InventoryService,
      ],
    });

    service = TestBed.inject(InventoryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Vérifie qu'aucune requête n'est restée en attente
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ---------- list ----------

  it('list() should GET /api/inventory', () => {
    service.list().subscribe(items => {
      expect(items).toEqual(mockItems);
      expect(items.length).toBe(2);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockItems);
  });

  it('list() should propagate errors', () => {
    let errorCaught: any = null;

    service.list().subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(baseUrl);
    req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });

    expect(errorCaught).toBeTruthy();
    expect(errorCaught.status).toBe(500);
  });

  // ---------- create ----------

  it('create() should POST to /api/inventory with the payload', () => {
    const payload: InventoryRequest = {
      name: 'Pain',
      quantity: 1,
      unit: 'PIECE',
      expiryDate: null,
    };
    const created = { ...mockItems[0], name: 'Pain' };

    service.create(payload).subscribe(item => {
      expect(item.name).toBe('Pain');
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(created);
  });


  // ---------- update ----------

  it('update() should PUT to /api/inventory/{id}', () => {
    const id = 'abc-123';
    const payload: InventoryRequest = {
      name: 'Lait entier',
      quantity: 2,
      unit: 'LITER',
      expiryDate: '2026-10-25',
    };
    const updated = { ...mockItems[0], ...payload };

    service.update(id, payload).subscribe(item => {
      expect(item.name).toBe('Lait entier');
    });

    const req = httpMock.expectOne(`${baseUrl}/${id}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);
    req.flush(updated);
  });

  // ---------- delete ----------

  it('delete() should DELETE /api/inventory/{id}', () => {
    const id = 'abc-123';
    let completed = false;

    service.delete(id).subscribe(() => (completed = true));

    const req = httpMock.expectOne(`${baseUrl}/${id}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    expect(completed).toBeTrue();
  });

  it('delete() should propagate errors', () => {
    const id = 'abc-123';
    let errorCaught: any = null;

    service.delete(id).subscribe({
      next: () => fail('should have failed'),
      error: (err) => (errorCaught = err),
    });

    const req = httpMock.expectOne(`${baseUrl}/${id}`);
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });

    expect(errorCaught.status).toBe(403);
  });

  // ---------- vérif URLs ----------

  it('should use the correct endpoint for every method', () => {
    service.list().subscribe();
    const listReq = httpMock.expectOne(baseUrl);
    expect(listReq.request.method).toBe('GET');
    listReq.flush([]);

    service.create({ name: 'X', quantity: 1, unit: 'PIECE', expiryDate: null }).subscribe();
    const createReq = httpMock.expectOne(baseUrl);
    expect(createReq.request.method).toBe('POST');
    createReq.flush({});

    service.update('id-1', { name: 'X', quantity: 1, unit: 'PIECE', expiryDate: null }).subscribe();
    const updateReq = httpMock.expectOne(`${baseUrl}/id-1`);
    expect(updateReq.request.method).toBe('PUT');
    updateReq.flush({});

    service.delete('id-2').subscribe();
    const deleteReq = httpMock.expectOne(`${baseUrl}/id-2`);
    expect(deleteReq.request.method).toBe('DELETE');
    deleteReq.flush(null);
  });

  it('create() should send the Content-Type header', () => {
  const payload: InventoryRequest = {
    name: 'Pain',
    quantity: 1,
    unit: 'PIECE',
    expiryDate: null,
  };

  service.create(payload).subscribe();

  const req = httpMock.expectOne(baseUrl);
  expect(req.request.headers.get('Content-Type')).toContain('application/json');
  req.flush({});
});

});