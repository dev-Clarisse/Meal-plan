import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { UserService } from '../services/user.service';

// Le rôle n'est pas dans le JWT : on le lit via GET /api/users/me.
// Ce guard ne sert qu'à l'affichage ; la vraie protection est côté back (/api/admin/** => ADMIN).
export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  if (!inject(AuthService).isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }
  return inject(UserService).getMe().pipe(
    map((user) => (user.role === 'ADMIN' ? true : router.createUrlTree(['/home']))),
    catchError(() => of(router.createUrlTree(['/login'])))
  );
};
