import { formatQuantity } from './quantity.pipe';

describe('formatQuantity', () => {
  it('affiche les pièces sans décimales et au pluriel', () => {
    expect(formatQuantity(2, 'pièce')).toBe('2 pièces');
    expect(formatQuantity(1, 'pièce')).toBe('1 pièce');
  });

  it('passe en kg et en L à partir de 1000', () => {
    expect(formatQuantity(1500, 'g')).toBe('1,5 kg');
    expect(formatQuantity(2000, 'mL')).toBe('2 L');
  });

  it('garde g et mL en dessous de 1000, sans zéros inutiles', () => {
    expect(formatQuantity(120, 'g')).toBe('120 g');
    expect(formatQuantity(250, 'mL')).toBe('250 mL');
    expect(formatQuantity(12.5, 'g')).toBe('12,5 g');
  });
});
