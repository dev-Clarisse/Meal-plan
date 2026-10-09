import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { InventoryComponent } from './inventory.component';
import { InventoryService } from '../../core/services/inventory.service';
import { InventoryItem } from '../../core/models/inventory.model';

describe('InventoryComponent', () => {
  let component: InventoryComponent;
  let fixture: ComponentFixture<InventoryComponent>;
  let inventoryServiceSpy: jasmine.SpyObj<InventoryService>;

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
    {
      id: '3',
      name: 'Fromage',
      quantity: 1,
      unit: 'PIECE',
      expiryDate: '2026-10-01',
      status: 'EXPIRED',
      createdAt: '2026-10-01T10:00:00Z',
      updatedAt: '2026-10-01T10:00:00Z',
    },
  ];

  beforeEach(async () => {
    inventoryServiceSpy = jasmine.createSpyObj('InventoryService', [
      'list', 'create', 'update', 'delete',
    ]);
    inventoryServiceSpy.list.and.returnValue(of(mockItems));

    await TestBed.configureTestingModule({
      imports: [InventoryComponent, FormsModule],
      providers: [
        { provide: InventoryService, useValue: inventoryServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InventoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // ---------- Chargement initial ----------

  it('should load items on init', () => {
    expect(inventoryServiceSpy.list).toHaveBeenCalledTimes(1);
    expect(component.items()).toEqual(mockItems);
    expect(component.loading()).toBeFalse();
  });

  it('should handle list error', () => {
    inventoryServiceSpy.list.and.returnValue(throwError(() => new Error('boom')));
    component.load();
    expect(component.error()).toBe('Erreur de chargement');
    expect(component.loading()).toBeFalse();
  });

  // ---------- Affichage ----------

  it('should render one row per item', () => {
    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(3);
  });

  it('should render the item names', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Lait');
    expect(compiled.textContent).toContain('Yaourt');
    expect(compiled.textContent).toContain('Fromage');
  });

  it('should show "aucun article" when list is empty', () => {
    component.items.set([]);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Aucun article');
  });

  // ---------- Création ----------

  it('should create an item when form is submitted', () => {
    inventoryServiceSpy.create.and.returnValue(of(mockItems[0]));
    inventoryServiceSpy.list.calls.reset();

    component.form = {
      name: 'Pain',
      quantity: 1,
      unit: 'PIECE',
      expiryDate: null,
    };
    component.submit();

    expect(inventoryServiceSpy.create).toHaveBeenCalledWith({
      name: 'Pain',
      quantity: 1,
      unit: 'PIECE',
      expiryDate: null,
    });
    expect(inventoryServiceSpy.list).toHaveBeenCalledTimes(1);
  });

  it('should not create if name is empty', () => {
    component.form = { name: '   ', quantity: 1, unit: 'PIECE', expiryDate: null };
    component.submit();

    expect(inventoryServiceSpy.create).not.toHaveBeenCalled();
  });

  it('should reset the form after creation', () => {
    inventoryServiceSpy.create.and.returnValue(of(mockItems[0]));

    component.form = { name: 'Pain', quantity: 3, unit: 'PIECE', expiryDate: null };
    component.submit();

    expect(component.form.name).toBe('');
    expect(component.form.quantity).toBe(1);
    expect(component.form.unit).toBe('PIECE');
    expect(component.editingId()).toBeNull();
  });

  // ---------- Édition ----------

  it('should populate form when editing', () => {
    component.edit(mockItems[1]);

    expect(component.editingId()).toBe('2');
    expect(component.form.name).toBe('Yaourt');
    expect(component.form.quantity).toBe(2);
    expect(component.form.unit).toBe('PIECE');
    expect(component.form.expiryDate).toBe('2026-10-11');
  });

  it('should call update when editing', () => {
    inventoryServiceSpy.update.and.returnValue(of(mockItems[0]));

    component.edit(mockItems[0]);
    component.form.name = 'Lait entier';
    component.submit();

    expect(inventoryServiceSpy.update).toHaveBeenCalledWith('1', {
      name: 'Lait entier',
      quantity: 1.5,
      unit: 'LITER',
      expiryDate: '2026-10-20',
    });
  });

  it('should cancel editing', () => {
    component.edit(mockItems[0]);
    component.resetForm();

    expect(component.editingId()).toBeNull();
    expect(component.form.name).toBe('');
  });

  // ---------- Suppression ----------

  it('should delete an item after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    inventoryServiceSpy.delete.and.returnValue(of(void 0));
    inventoryServiceSpy.list.calls.reset();

    component.remove('1');

    expect(inventoryServiceSpy.delete).toHaveBeenCalledWith('1');
    expect(inventoryServiceSpy.list).toHaveBeenCalledTimes(1);
  });

  it('should not delete when user cancels', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.remove('1');

    expect(inventoryServiceSpy.delete).not.toHaveBeenCalled();
  });

  // ---------- Statut / badge ----------

  it('should return correct CSS class for each status', () => {
    expect(component.statusClass('EXPIRED')).toBe('badge-expired');
    expect(component.statusClass('EXPIRING_SOON')).toBe('badge-soon');
    expect(component.statusClass('OK')).toBe('badge-ok');
    expect(component.statusClass('NO_DATE')).toBe('badge-none');
  });

  it('should return correct label for each status', () => {
    expect(component.statusLabel('EXPIRED')).toContain('Périmé');
    expect(component.statusLabel('EXPIRING_SOON')).toContain('Bientôt');
    expect(component.statusLabel('OK')).toContain('OK');
    expect(component.statusLabel('NO_DATE')).toContain('Sans date');
  });

  it('should render the correct badge for each status', () => {
    const badges = fixture.nativeElement.querySelectorAll('.badge-status');
    expect(badges.length).toBe(3);
    expect(badges[0].classList).toContain('badge-ok');
    expect(badges[1].classList).toContain('badge-soon');
    expect(badges[2].classList).toContain('badge-expired');
  });
});