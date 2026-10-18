import { DragDropModule } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MealCalendarComponent } from './meal-calendar/meal-calendar.component';
import { ShoppingListComponent } from './shopping-list/shopping-list.component';

@NgModule({
  declarations: [MealCalendarComponent, ShoppingListComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DragDropModule,
    RouterModule.forChild([{ path: '', component: MealCalendarComponent }]),
  ],
})
export class MealPlannerModule {}