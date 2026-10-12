import { HttpInterceptorFn } from '@angular/common/http';

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