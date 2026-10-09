import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';
import { NotificationBellComponent } from '../../features/notifications/notification-bell.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, NotificationBellComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
  private auth = inject(AuthService);
  private users = inject(UserService);
  private router = inject(Router);

  user = signal<User | null>(null);

  ngOnInit(): void {
    this.users.getMe().subscribe(u => this.user.set(u));
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}