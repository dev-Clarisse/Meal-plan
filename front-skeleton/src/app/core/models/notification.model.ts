export type NotificationType = 'EXPIRING_SOON' | 'EXPIRED';

export interface Notification {
  id: string;
  inventoryId: string;
  inventoryName: string;
  type: NotificationType;
  message: string;
  read: boolean;
  createdAt: string;
}