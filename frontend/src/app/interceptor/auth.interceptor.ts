import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { mergeMap } from 'rxjs/operators';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(OidcSecurityService);

  console.log('[AuthInterceptor] Intercepting:', req.method, req.url);

  return authService.getAccessToken().pipe(
    mergeMap(token => {
      console.log('[AuthInterceptor] Token present?', !!token);

      if (token) {
        req = req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        });
        console.log('[AuthInterceptor] Authorization header added');
      }
      return next(req);
    })
  );
};
