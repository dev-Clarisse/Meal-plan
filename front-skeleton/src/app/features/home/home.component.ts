import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';

// Page provisoire après connexion
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container py-5 text-center">
      <h1 class="text-mp-green">Bienvenue 🥗</h1>
      <p *ngIf="user">Connecté en tant que <strong>{{ user.email }}</strong></p>
      <p>Ici viendra le planning des repas.</p>
      <button class="btn btn-outline-mp" (click)="logout()">Se déconnecter</button>
    </div>
  `,
})
export class HomeComponent implements OnInit {
  private auth = inject(AuthService);
  private users = inject(UserService);
  private router = inject(Router);

  user?: User;

  ngOnInit(): void {
    this.users.getMe().subscribe((u) => (this.user = u));
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
