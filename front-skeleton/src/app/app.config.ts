import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { jsonContentTypeInterceptor } from 'core/interceptors/json-centent-type.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([
        jsonContentTypeInterceptor,   // 1. pose le Content-Type d'abord
        authInterceptor,               // 2. ajoute l'Authorization + gère le 401
      ]),
    ),
  ],
};
