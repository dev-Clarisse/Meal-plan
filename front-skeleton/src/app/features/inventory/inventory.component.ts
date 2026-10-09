import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventoryService } from '../../core/services/inventory.service';
import { InventoryItem, InventoryRequest, Unit } from '../../core/models/inventory.model';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.scss'],
})
export class InventoryComponent implements OnInit {
  private svc = inject(InventoryService);

  items = signal<InventoryItem[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);

  // Formulaire
  form: InventoryRequest = { name: '', quantity: 1, unit: 'PIECE', expiryDate: null };
  editingId = signal<string | null>(null);

  units: Unit[] = ['GRAM','KILOGRAM','MILLILITER','LITER','PIECE','TABLESPOON','TEASPOON','CUP'];

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.list().subscribe({
      next: items => { this.items.set(items); this.loading.set(false); },
      error: () => { this.error.set('Erreur de chargement'); this.loading.set(false); }
    });
  }

  submit(): void {
    if (!this.form.name.trim()) return;
    const obs = this.editingId()
      ? this.svc.update(this.editingId()!, this.form)
      : this.svc.create(this.form);

    obs.subscribe(() => {
      this.resetForm();
      this.load();
    });
  }

  edit(item: InventoryItem): void {
    this.editingId.set(item.id);
    this.form = {
      name: item.name,
      quantity: item.quantity,
      unit: item.unit,
      expiryDate: item.expiryDate
    };
  }

  remove(id: string): void {
    if (!confirm('Supprimer cet article ?')) return;
    this.svc.delete(id).subscribe(() => this.load());
  }

  resetForm(): void {
    this.editingId.set(null);
    this.form = { name: '', quantity: 1, unit: 'PIECE', expiryDate: null };
  }

  statusClass(status: string): string {
    switch (status) {
      case 'EXPIRED':       return 'badge-expired';
      case 'EXPIRING_SOON': return 'badge-soon';
      case 'OK':            return 'badge-ok';
      default:              return 'badge-none';
    }
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'EXPIRED':       return '🔴 Périmé';
      case 'EXPIRING_SOON': return '🟠 Bientôt';
      case 'OK':            return '🟢 OK';
      default:              return '⚪ Sans date';
    }
  }
}