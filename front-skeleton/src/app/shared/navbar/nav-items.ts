export interface NavItem {
  label: string;
  path: string;
}

export const NAV_ITEMS: NavItem[] = [
  { label: 'Recettes', path: '/recettes' },
  { label: 'Inventaire', path: '/inventory' },
];
