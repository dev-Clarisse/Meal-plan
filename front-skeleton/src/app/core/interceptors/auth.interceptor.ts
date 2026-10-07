import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

// 1) Ajoute "Authorization: Bearer <jwt>" aux requêtes /api
// 2) Si le back répond 401 sur une route protégée (token expiré/invalide),
//    on déconnecte et on renvoie vers /login
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const isLogin = req.url.endsWith('/auth/login');

  const token = auth.getToken();
  if (token && req.url.startsWith('/api') && !isLogin) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !isLogin) {
        auth.logout();
        router.navigate(['/login']);
      }
      return throwError(() => err);
    })
  );
};
