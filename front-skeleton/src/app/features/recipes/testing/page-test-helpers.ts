import { EnvironmentProviders, Provider } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { ConfirmService } from '../../../shared/confirm-dialog/confirm.service';
import { ToastService } from '../../../shared/toast/toast.service';

export interface PageDoubles {
  confirm: jasmine.SpyObj<ConfirmService>;
  toast: jasmine.SpyObj<ToastService>;
}

export function createPageDoubles(): PageDoubles {
  const confirm = jasmine.createSpyObj<ConfirmService>('ConfirmService', ['ask']);
  confirm.ask.and.resolveTo(true);
  const toast = jasmine.createSpyObj<ToastService>('ToastService', ['success', 'error']);
  return { confirm, toast };
}

export function pageProviders(
  doubles: PageDoubles,
  routeParams: Record<string, string> = {},
): (Provider | EnvironmentProviders)[] {
  return [
    provideRouter([]),
    { provide: ConfirmService, useValue: doubles.confirm },
    { provide: ToastService, useValue: doubles.toast },
    {
      provide: ActivatedRoute,
      useValue: { snapshot: { paramMap: convertToParamMap(routeParams) } },
    },
  ];
}

export function spyOnNavigation(): jasmine.Spy<Router['navigate']> {
  return spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
}
