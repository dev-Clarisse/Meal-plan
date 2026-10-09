export type Unit =
  | 'GRAM' | 'KILOGRAM' | 'MILLILITER' | 'LITER'
  | 'PIECE' | 'TABLESPOON' | 'TEASPOON' | 'CUP';

export type ExpiryStatus = 'OK' | 'EXPIRING_SOON' | 'EXPIRED' | 'NO_DATE';

export interface InventoryItem {
  id: string;
  name: string;
  quantity: number;
  unit: Unit;
  expiryDate: string | null;
  status: ExpiryStatus;
  createdAt: string;
  updatedAt: string;
}

export interface InventoryRequest {
  name: string;
  quantity: number;
  unit: Unit;
  expiryDate: string | null;
}