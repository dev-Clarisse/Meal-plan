import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { UserService } from '../services/user.service';

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  if (!inject(AuthService).isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }
  return inject(UserService).getMe().pipe(
    map((user) => (user.role === 'ADMIN' ? true : router.createUrlTree(['/recettes']))),
    catchError(() => of(router.createUrlTree(['/login'])))
  );
};
