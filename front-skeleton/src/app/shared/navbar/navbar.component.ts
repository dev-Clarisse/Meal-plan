import { Component, HostListener, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { NotificationBellComponent } from '../../features/notifications/notification-bell.component';

const TRANSPARENT_PATHS = ['/', '/recettes'];
import { NAV_ITEMS } from './nav-items';

const AUTH_PATHS = ['/login', '/register'];

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, NotificationBellComponent],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
})
export class NavbarComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  readonly items = NAV_ITEMS;
  readonly isLoggedIn = toSignal(this.authService.isLoggedIn$, { initialValue: false });

  readonly transparent = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => TRANSPARENT_PATHS.includes(event.urlAfterRedirects.split('?')[0])),
      takeUntilDestroyed()
    ),
    { initialValue: TRANSPARENT_PATHS.includes(this.router.url.split('?')[0]) }
  );

  readonly authPage = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => AUTH_PATHS.includes(event.urlAfterRedirects.split('?')[0])),
      takeUntilDestroyed()
    ),
    { initialValue: AUTH_PATHS.includes(this.router.url.split('?')[0]) }
  );

  readonly scrolled = signal(false);

  @HostListener('window:scroll')
  onScroll(): void {
    this.scrolled.set(window.scrollY > 24);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
