import { Component, Input, OnChanges, inject } from '@angular/core';
import { ShoppingItem, ShoppingList } from '../meal-planner.model';
import { MealPlannerService } from '../meal-planner.service';
import { QuantityPipe } from '../quantity.pipe';

@Component({
  selector: 'app-shopping-list',
  standalone: true,
  imports: [QuantityPipe],
  templateUrl: './shopping-list.component.html',
})
export class ShoppingListComponent implements OnChanges {
  private readonly service = inject(MealPlannerService);

  @Input({ required: true }) planId!: number;

  list: ShoppingList | null = null;
  error = '';

  ngOnChanges(): void {
    this.list = null;
    this.error = '';
    // 404 = liste pas encore générée : ce n'est pas une erreur
    this.service.getShoppingList(this.planId).subscribe({
      next: (l) => (this.list = l),
      error: () => (this.list = null),
    });
  }

  generate(): void {
    this.error = '';
    this.service.generateShoppingList(this.planId).subscribe({
      next: (l) => (this.list = l),
      error: () => (this.error = 'Génération de la liste impossible'),
    });
  }

  toggle(item: ShoppingItem): void {
    const purchased = !item.purchased;
    item.purchased = purchased;
    this.service.setPurchased(item.id, purchased).subscribe({
      error: () => {
        item.purchased = !purchased;
        this.error = 'Mise à jour impossible';
      },
    });
  }
}