import { Pipe, PipeTransform } from '@angular/core';

const NUMBER = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 });

/**
 * Le backend renvoie les quantités en unité de base (g, mL, pièce) avec 3 décimales.
 * On les rend lisibles : 1500 g -> 1,5 kg ; 2000 mL -> 2 L ; 2.000 pièce -> 2 pièces.
 */
export function formatQuantity(quantity: number, unit: string): string {
  if (unit === 'g' && quantity >= 1000) {
    return `${NUMBER.format(quantity / 1000)} kg`;
  }
  if (unit === 'mL' && quantity >= 1000) {
    return `${NUMBER.format(quantity / 1000)} L`;
  }
  if (unit === 'pièce') {
    return `${NUMBER.format(quantity)} ${quantity > 1 ? 'pièces' : 'pièce'}`;
  }
  return `${NUMBER.format(quantity)} ${unit}`;
}

@Pipe({ name: 'quantity', standalone: true })
export class QuantityPipe implements PipeTransform {
  transform(quantity: number, unit: string): string {
    return formatQuantity(quantity, unit);
  }
}
