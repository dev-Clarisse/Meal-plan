import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../core/services/notification.service';
import { Notification } from '../../core/models/notification.model';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bell-wrapper">
      <button class="btn bell-btn" (click)="toggle()">
        🔔
        @if (unread() > 0) {
          <span class="badge-count">{{ unread() }}</span>
        }
      </button>

      @if (open()) {
        <div class="dropdown-panel">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <strong>Notifications</strong>
            @if (unread() > 0) {
              <button class="btn btn-sm btn-link p-0" (click)="markAll()">Tout lire</button>
            }
          </div>

          @if (items().length === 0) {
            <p class="text-muted small mb-0">Aucune notification.</p>
          } @else {
            @for (n of items(); track n.id) {
              <div class="notif-item" [class.unread]="!n.read" (click)="markRead(n)">
                <div class="notif-msg">{{ n.message }}</div>
                <small class="text-muted">{{ n.createdAt | date:'short' }}</small>
              </div>
            }
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .bell-wrapper { position: relative; }
    .bell-btn {
      position: relative; font-size: 1.4rem;
      background: transparent; border: none;
    }
    .badge-count {
      position: absolute; top: -4px; right: -4px;
      background: #e74c3c; color: white;
      border-radius: 50%; font-size: .7rem;
      padding: 2px 6px; font-weight: 700;
    }
    .dropdown-panel {
      position: absolute; right: 0; top: 110%;
      width: 320px; max-height: 420px; overflow-y: auto;
      background: white; border: 1px solid var(--mp-green-light);
      border-radius: .75rem; padding: .75rem;
      box-shadow: 0 4px 16px rgba(0,0,0,.12); z-index: 1000;
    }
    .notif-item {
      padding: .5rem; border-radius: .5rem; cursor: pointer;
      &:hover { background: var(--mp-green-light); }
      &.unread { background: var(--mp-yellow-light); }
    }
    .notif-msg { font-size: .9rem; color: #2b3a1f; }
  `]
})
export class NotificationBellComponent implements OnInit {
  private svc = inject(NotificationService);

  items = signal<Notification[]>([]);
  unread = signal(0);
  open = signal(false);

  ngOnInit(): void {
    this.refresh();
    // Polling simple toutes les 60s (à remplacer par WebSocket plus tard)
    setInterval(() => this.refresh(), 60_000);
  }

  refresh(): void {
    this.svc.list().subscribe(n => this.items.set(n));
    this.svc.unreadCount().subscribe(c => this.unread.set(c));
  }

  toggle(): void { this.open.update(v => !v); }

  markRead(n: Notification): void {
    if (n.read) return;
    this.svc.markAsRead(n.id).subscribe(() => this.refresh());
  }

  markAll(): void {
    this.svc.markAllAsRead().subscribe(() => this.refresh());
  }
}