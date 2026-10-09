import { ComponentFixture, TestBed, discardPeriodicTasks, fakeAsync, tick } from '@angular/core/testing';
import { of } from 'rxjs';
import { NotificationBellComponent } from './notification-bell.component';
import { NotificationService } from '../../core/services/notification.service';
import { Notification } from '../../core/models/notification.model';

describe('NotificationBellComponent', () => {
  let component: NotificationBellComponent;
  let fixture: ComponentFixture<NotificationBellComponent>;
  let notifServiceSpy: jasmine.SpyObj<NotificationService>;

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

  beforeEach(async () => {
    notifServiceSpy = jasmine.createSpyObj('NotificationService', [
      'list', 'unreadCount', 'markAsRead', 'markAllAsRead',
    ]);
    notifServiceSpy.list.and.returnValue(of(mockNotifs));
    notifServiceSpy.unreadCount.and.returnValue(of(1));
    notifServiceSpy.markAsRead.and.returnValue(of(void 0));
    notifServiceSpy.markAllAsRead.and.returnValue(of(void 0));

    await TestBed.configureTestingModule({
      imports: [NotificationBellComponent],
      providers: [
        { provide: NotificationService, useValue: notifServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationBellComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // ---------- Chargement initial ----------

  it('should load notifications and unread count on init', () => {
    expect(notifServiceSpy.list).toHaveBeenCalled();
    expect(notifServiceSpy.unreadCount).toHaveBeenCalled();
    expect(component.items()).toEqual(mockNotifs);
    expect(component.unread()).toBe(1);
  });

  it('should display the bell icon', () => {
    const btn = fixture.nativeElement.querySelector('.bell-btn');
    expect(btn).toBeTruthy();
    expect(btn.textContent).toContain('🔔');
  });

  it('should display the unread badge when count > 0', () => {
    const badge = fixture.nativeElement.querySelector('.badge-count');
    expect(badge).toBeTruthy();
    expect(badge.textContent.trim()).toBe('1');
  });

  it('should hide the badge when count is 0', () => {
    component.unread.set(0);
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.badge-count');
    expect(badge).toBeFalsy();
  });

  // ---------- Ouverture / fermeture ----------

  it('should start with the panel closed', () => {
    expect(component.open()).toBeFalse();
    expect(fixture.nativeElement.querySelector('.dropdown-panel')).toBeFalsy();
  });

  it('should toggle panel on bell click', () => {
    const btn: HTMLButtonElement = fixture.nativeElement.querySelector('.bell-btn');
    btn.click();
    fixture.detectChanges();

    expect(component.open()).toBeTrue();
    expect(fixture.nativeElement.querySelector('.dropdown-panel')).toBeTruthy();

    btn.click();
    fixture.detectChanges();
    expect(component.open()).toBeFalse();
  });

  // ---------- Affichage des notifications ----------

  it('should display all notifications when open', () => {
    component.toggle();
    fixture.detectChanges();

    const items = fixture.nativeElement.querySelectorAll('.notif-item');
    expect(items.length).toBe(2);
    expect(fixture.nativeElement.textContent).toContain('Le produit Lait expire bientôt');
    expect(fixture.nativeElement.textContent).toContain('Le produit Fromage a expiré');
  });

  it('should mark unread notifications with the unread class', () => {
    component.toggle();
    fixture.detectChanges();

    const items = fixture.nativeElement.querySelectorAll('.notif-item');
    expect(items[0].classList).toContain('unread');
    expect(items[1].classList).not.toContain('unread');
  });

  it('should show "aucune notification" when empty', () => {
    component.items.set([]);
    component.toggle();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Aucune notification');
  });

  // ---------- Actions ----------

  it('should mark a notification as read on click', () => {
    component.markRead(mockNotifs[0]);
    expect(notifServiceSpy.markAsRead).toHaveBeenCalledWith('n1');
  });

  it('should not call markAsRead if already read', () => {
    component.markRead(mockNotifs[1]);
    expect(notifServiceSpy.markAsRead).not.toHaveBeenCalled();
  });

  it('should mark all notifications as read', () => {
    component.markAll();
    expect(notifServiceSpy.markAllAsRead).toHaveBeenCalled();
  });

  it('should refresh after markAll', () => {
    notifServiceSpy.list.calls.reset();
    notifServiceSpy.unreadCount.calls.reset();

    component.markAll();

    expect(notifServiceSpy.list).toHaveBeenCalledTimes(1);
    expect(notifServiceSpy.unreadCount).toHaveBeenCalledTimes(1);
  });

  it('should display "Tout lire" only when there are unread notifications', () => {
    component.unread.set(0);
    component.toggle();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('Tout lire');

    component.unread.set(2);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Tout lire');
  });

  // ---------- Polling ----------

  it('should refresh notifications periodically', fakeAsync(() => {
    // Recrée le composant dans la zone fakeAsync pour capturer le setInterval
    const newFixture = TestBed.createComponent(NotificationBellComponent);
    newFixture.detectChanges();

    notifServiceSpy.list.calls.reset();
    notifServiceSpy.unreadCount.calls.reset();

    tick(60_000);

    expect(notifServiceSpy.list).toHaveBeenCalled();
    expect(notifServiceSpy.unreadCount).toHaveBeenCalled();

    // Nettoie le setInterval pour éviter les fuites entre tests
    newFixture.destroy();
    discardPeriodicTasks();
  }));
});