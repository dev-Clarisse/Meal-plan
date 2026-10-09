import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Force Content-Type: application/json sur les requêtes mutantes (POST/PUT/PATCH)
 * qui envoient un body objet et n'ont pas déjà de Content-Type.
 *
 * Nécessaire car Angular 18+ ne le pose plus automatiquement.
 * Sans cet en-tête, Spring Boot refuse le body avec un 415 Unsupported Media Type.
 */
export const jsonContentTypeInterceptor: HttpInterceptorFn = (req, next) => {
  const isMutation = ['POST', 'PUT', 'PATCH'].includes(req.method);
  const isFormData = req.body instanceof FormData;
  const hasBody = req.body !== null && req.body !== undefined;
  const hasContentType = req.headers.has('Content-Type');

  if (isMutation && hasBody && !isFormData && !hasContentType) {
    req = req.clone({
      setHeaders: { 'Content-Type': 'application/json' },
    });
  }

  return next(req);
};