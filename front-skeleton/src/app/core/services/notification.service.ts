import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);
  private base = '/api/notifications';

  list(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.base);
  }

  unreadCount(): Observable<number> {
    return this.http.get<{ count: number }>(`${this.base}/unread-count`)
                    .pipe(map(r => r.count));
  }

  markAsRead(id: string): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/read`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.http.post<void>(`${this.base}/read-all`, {});
  }
}