import { Component, Input, OnChanges } from '@angular/core';
import { ShoppingItem, ShoppingList } from '../meal-planner.model';
import { MealPlannerService } from '../meal-planner.service';

@Component({
  selector: 'app-shopping-list',
  templateUrl: './shopping-list.component.html',
})
export class ShoppingListComponent implements OnChanges {
  @Input({ required: true }) planId!: number;

  list: ShoppingList | null = null;
  error = '';

  constructor(private service: MealPlannerService) {}

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