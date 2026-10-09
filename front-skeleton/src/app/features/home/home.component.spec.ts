import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { HomeComponent } from './home.component';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { NotificationService } from '../../core/services/notification.service';
import { User } from '../../core/models/user.model';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let notifServiceSpy: jasmine.SpyObj<NotificationService>;
  let navigateSpy: jasmine.Spy;

  const mockUser: User = {
    id: 'uuid-123',
    email: 'user@test.com',
    role: 'USER',
    consentGivenAt: '2026-01-01T09:00:00Z',
    createdAt: '2026-01-01T09:00:00Z',
    updatedAt: '2026-01-01T09:00:00Z',
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['logout']);
    userServiceSpy = jasmine.createSpyObj('UserService', ['getMe']);
    notifServiceSpy = jasmine.createSpyObj('NotificationService', [
      'list', 'unreadCount', 'markAsRead', 'markAllAsRead',
    ]);

    userServiceSpy.getMe.and.returnValue(of(mockUser));
    notifServiceSpy.list.and.returnValue(of([]));
    notifServiceSpy.unreadCount.and.returnValue(of(0));

    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [
        provideRouter([]),                                // fournit le VRAI Router + ActivatedRoute
        { provide: AuthService, useValue: authServiceSpy },
        { provide: UserService, useValue: userServiceSpy },
        { provide: NotificationService, useValue: notifServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;

    // Spy sur la méthode navigate du VRAI Router (au lieu de l'écraser)
    navigateSpy = spyOn(TestBed.inject(Router), 'navigate');

    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();     // stoppe le setInterval du NotificationBell
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load user on init', () => {
    expect(userServiceSpy.getMe).toHaveBeenCalledTimes(1);
    expect(component.user()).toEqual(mockUser);
  });

  it('should display the user email in the template', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('user@test.com');
  });

  it('should display the welcome message', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Bienvenue');
  });

  it('should call logout and navigate to /login', () => {
    component.logout();

    expect(authServiceSpy.logout).toHaveBeenCalledTimes(1);
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should render a link to /inventory', () => {
    const link = fixture.nativeElement.querySelector('a[routerLink="/inventory"]');
    expect(link).toBeTruthy();
  });

  it('should render the notification bell component', () => {
    const bell = fixture.nativeElement.querySelector('app-notification-bell');
    expect(bell).toBeTruthy();
  });
});