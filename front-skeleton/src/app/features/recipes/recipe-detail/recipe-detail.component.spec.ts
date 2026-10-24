import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { RecipeService } from '../services/recipe.service';
import { buildRecipe } from '../testing/recipe.fixtures';
import {
  PageDoubles,
  createPageDoubles,
  pageProviders,
  spyOnNavigation,
} from '../testing/page-test-helpers';
import { RecipeDetailComponent } from './recipe-detail.component';

describe('RecipeDetailComponent', () => {
  let fixture: ComponentFixture<RecipeDetailComponent>;
  let component: RecipeDetailComponent;
  let recipeService: jasmine.SpyObj<RecipeService>;
  let doubles: PageDoubles;
  let navigate: ReturnType<typeof spyOnNavigation>;

  const recipe = buildRecipe({ id: 5 });
  const page = (): HTMLElement => fixture.nativeElement;

  function create(): void {
    TestBed.configureTestingModule({
      imports: [RecipeDetailComponent],
      providers: [
        { provide: RecipeService, useValue: recipeService },
        ...pageProviders(doubles, { id: '5' }),
      ],
    });
    navigate = spyOnNavigation();
    fixture = TestBed.createComponent(RecipeDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(() => {
    recipeService = jasmine.createSpyObj<RecipeService>('RecipeService', ['findById', 'delete']);
    recipeService.findById.and.returnValue(of(recipe));
    recipeService.delete.and.returnValue(of(undefined));
    doubles = createPageDoubles();
  });

  it('loads the recipe from the route id', () => {
    create();

    expect(recipeService.findById).toHaveBeenCalledOnceWith(5);
    expect(component.recipe()).toEqual(recipe);
    expect(component.loading()).toBeFalse();
  });

  it('renders the recipe title', () => {
    create();

    expect(page().querySelector('h1')?.textContent?.trim()).toBe('Crepes');
  });

  it('renders one list item per ingredient', () => {
    create();

    expect(page().querySelectorAll('.ingredients li').length).toBe(recipe.ingredients.length);
  });

  it('shows a not found message on a 404', () => {
    recipeService.findById.and.returnValue(throwError(() => ({ status: 404 })));

    create();

    expect(component.error()).toBe('Recette introuvable.');
    expect(component.loading()).toBeFalse();
  });

  it('shows a loading error on any other failure', () => {
    recipeService.findById.and.returnValue(throwError(() => ({ status: 500 })));

    create();

    expect(component.error()).toBe('Erreur de chargement.');
  });

  it('asks for a dangerous confirmation when clicking the delete button', async () => {
    create();

    (page().querySelector('button.danger') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(doubles.confirm.ask).toHaveBeenCalledWith(jasmine.objectContaining({ tone: 'danger' }));
  });

  it('deletes the recipe after confirmation', async () => {
    create();

    await component.remove();

    expect(recipeService.delete).toHaveBeenCalledOnceWith(5);
    expect(doubles.toast.success).toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith(['/recettes']);
  });

  it('keeps the recipe when the user cancels', async () => {
    doubles.confirm.ask.and.resolveTo(false);
    create();

    await component.remove();

    expect(recipeService.delete).not.toHaveBeenCalled();
  });

  it('shows an error toast when the deletion fails', async () => {
    recipeService.delete.and.returnValue(throwError(() => new Error('fail')));
    create();

    await component.remove();

    expect(doubles.toast.error).toHaveBeenCalled();
    expect(navigate).not.toHaveBeenCalled();
  });

  it('does nothing when no recipe is loaded', async () => {
    recipeService.findById.and.returnValue(throwError(() => ({ status: 404 })));
    create();

    await component.remove();

    expect(doubles.confirm.ask).not.toHaveBeenCalled();
  });
});
