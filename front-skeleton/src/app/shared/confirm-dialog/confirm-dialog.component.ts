import { Component, ElementRef, ViewChild, effect, inject } from '@angular/core';
import { ConfirmService } from './confirm.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.scss',
})
export class ConfirmDialogComponent {
  protected readonly confirm = inject(ConfirmService);

  @ViewChild('dialog', { static: true }) private dialog!: ElementRef<HTMLDialogElement>;

  constructor() {
    effect(() => {
      const element = this.dialog?.nativeElement;
      if (!element) {
        return;
      }
      if (this.confirm.options() && !element.open) {
        element.showModal();
      } else if (!this.confirm.options() && element.open) {
        element.close();
      }
    });
  }

  protected onCancel(event: Event): void {
    event.preventDefault();
    this.confirm.answer(false);
  }
}
