// Intl plutôt que DatePipe : la locale 'fr' n'est pas enregistrée dans l'application
const LONG_DATE = new Intl.DateTimeFormat('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' });
const SHORT_DATE = new Intl.DateTimeFormat('fr-FR', { day: 'numeric', month: 'short' });

export function toIso(d: Date): string {
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
}

export function mondayOf(d: Date): Date {
  const c = new Date(d);
  c.setDate(c.getDate() - ((c.getDay() + 6) % 7));
  return c;
}

// Lundi de la semaine passée dans l'URL (?semaine=AAAA-MM-JJ), sinon de la semaine en cours
export function weekFromParam(param: string | null): Date {
  const parsed = param && /^\d{4}-\d{2}-\d{2}$/.test(param) ? new Date(`${param}T00:00:00`) : null;
  return mondayOf(parsed && !isNaN(parsed.getTime()) ? parsed : new Date());
}

export function addDays(d: Date, days: number): Date {
  const c = new Date(d);
  c.setDate(c.getDate() + days);
  return c;
}

export function longDate(d: Date): string {
  return LONG_DATE.format(d);
}

export function shortDate(d: Date): string {
  return SHORT_DATE.format(d);
}
