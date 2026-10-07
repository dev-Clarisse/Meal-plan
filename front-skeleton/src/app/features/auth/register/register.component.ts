import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { switchMap } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const pwd = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return pwd === confirm ? null : { mismatch: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  // Même habillage que la page de connexion
  styleUrls: ['../login/login.component.scss'],
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private users = inject(UserService);
  private auth = inject(AuthService);
  private router = inject(Router);

  loading = false;
  showPassword = false;
  errorMessage = '';

  // Contraintes alignées sur UserCreateRequest (email<=254, mdp 12..72, consentement obligatoire)
  form = this.fb.nonNullable.group(
    {
      email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
      password: ['', [Validators.required, Validators.minLength(12), Validators.maxLength(72)]],
      confirmPassword: ['', Validators.required],
      consent: [false, Validators.requiredTrue],
    },
    { validators: passwordsMatch }
  );

  get email() { return this.form.controls.email; }
  get password() { return this.form.controls.password; }
  get confirmPassword() { return this.form.controls.confirmPassword; }
  get consent() { return this.form.controls.consent; }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    const { email, password, consent } = this.form.getRawValue();

    // Inscription puis connexion automatique
    this.users
      .register({ email, password, consent })
      .pipe(switchMap(() => this.auth.login({ email, password })))
      .subscribe({
        next: () => this.router.navigate(['/home']),
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          this.errorMessage =
            err.status === 400
              ? 'Les informations saisies sont invalides.'
              : err.status === 409
              ? "Impossible de créer le compte avec cet email."
              : 'Impossible de joindre le serveur. Réessaie plus tard.';
        },
      });
  }
}
