import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Ingredient } from '../models/ingredient.model';
import { IngredientService } from '../services/ingredient.service';
import { RecipeService } from '../services/recipe.service';
import { buildRecipe, buildRecipeRequest } from '../testing/recipe.fixtures';
import {
  PageDoubles,
  createPageDoubles,
  pageProviders,
  spyOnNavigation,
} from '../testing/page-test-helpers';
import { RecipeFormComponent } from './recipe-form.component';

describe('RecipeFormComponent', () => {
  let fixture: ComponentFixture<RecipeFormComponent>;
  let component: RecipeFormComponent;
  let recipeService: jasmine.SpyObj<RecipeService>;
  let ingredientService: jasmine.SpyObj<IngredientService>;
  let doubles: PageDoubles;
  let navigate: ReturnType<typeof spyOnNavigation>;

  const ingredients: Ingredient[] = [
    { id: 1, name: 'Flour', defaultUnit: 'G' },
    { id: 2, name: 'Milk', defaultUnit: 'ML' },
  ];

  const page = (): HTMLElement => fixture.nativeElement;
  const lineCount = (): number => page().querySelectorAll('.line').length;

  function click(selector: string, index = 0): void {
    (page().querySelectorAll(selector)[index] as HTMLElement).click();
    fixture.detectChanges();
  }

  function create(id: string | null = null): void {
    TestBed.configureTestingModule({
      imports: [RecipeFormComponent],
      providers: [
        { provide: RecipeService, useValue: recipeService },
        { provide: IngredientService, useValue: ingredientService },
        ...pageProviders(doubles, id ? { id } : {}),
      ],
    });
    navigate = spyOnNavigation();
    fixture = TestBed.createComponent(RecipeFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  function fillValidForm(): void {
    component.form.patchValue({ title: 'Crepes', servings: 4, instructions: 'Mix and cook.' });
    component.lines.at(0).patchValue({ ingredientId: 1, quantity: 250, unit: 'G' });
  }

  beforeEach(() => {
    recipeService = jasmine.createSpyObj<RecipeService>('RecipeService', ['findById', 'create', 'update']);
    ingredientService = jasmine.createSpyObj<IngredientService>('IngredientService', ['search']);
    ingredientService.search.and.returnValue(of(ingredients));
    doubles = createPageDoubles();
  });

  it('loads the ingredient catalog on init', () => {
    create();

    expect(ingredientService.search).toHaveBeenCalled();
    expect(component.ingredients()).toEqual(ingredients);
  });

  it('starts with one empty ingredient line and an invalid form', () => {
    create();

    expect(lineCount()).toBe(1);
    expect(component.form.invalid).toBeTrue();
    expect(component.recipeId).toBeNull();
  });

  it('lists the catalog ingredients and a placeholder in the ingredient select', () => {
    create();

    const select = page().querySelector('.line select') as HTMLSelectElement;
    expect(select.options.length).toBe(ingredients.length + 1);
  });

  it('adds a line when clicking the add button', () => {
    create();

    click('button.add');

    expect(lineCount()).toBe(2);
  });

  it('disables the remove button while only one line is left', () => {
    create();
    const removeButton = () => page().querySelector('.line .ghost') as HTMLButtonElement;

    expect(removeButton().disabled).toBeTrue();

    click('button.add');

    expect(removeButton().disabled).toBeFalse();
  });

  it('removes a line when clicking its remove button', () => {
    create();
    click('button.add');

    click('.line .ghost');

    expect(lineCount()).toBe(1);
  });

  it('does not ask for confirmation when the form is invalid', async () => {
    create();

    await component.submit();

    expect(doubles.confirm.ask).not.toHaveBeenCalled();
    expect(recipeService.create).not.toHaveBeenCalled();
  });

  it('creates the recipe after confirmation', async () => {
    recipeService.create.and.returnValue(of(buildRecipe({ id: 9 })));
    create();
    fillValidForm();

    await component.submit();

    expect(recipeService.create).toHaveBeenCalledOnceWith(buildRecipeRequest());
    expect(doubles.toast.success).toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith(['/recettes', 9], { replaceUrl: true });
  });

  it('submits the form when clicking the save button', async () => {
    recipeService.create.and.returnValue(of(buildRecipe({ id: 9 })));
    create();
    fillValidForm();

    click('button[type="submit"]');
    await fixture.whenStable();

    expect(recipeService.create).toHaveBeenCalledOnceWith(buildRecipeRequest());
  });

  it('does not create the recipe when the user cancels', async () => {
    doubles.confirm.ask.and.resolveTo(false);
    create();
    fillValidForm();

    await component.submit();

    expect(recipeService.create).not.toHaveBeenCalled();
    expect(navigate).not.toHaveBeenCalled();
  });

  it('shows an error when saving fails', async () => {
    recipeService.create.and.returnValue(throwError(() => new Error('fail')));
    create();
    fillValidForm();

    await component.submit();

    expect(doubles.toast.error).toHaveBeenCalled();
    expect(component.error()).not.toBeNull();
    expect(component.saving()).toBeFalse();
  });

  it('loads the recipe in edit mode', () => {
    recipeService.findById.and.returnValue(of(buildRecipe({ id: 7 })));

    create('7');

    expect(recipeService.findById).toHaveBeenCalledOnceWith(7);
    expect(component.recipeId).toBe(7);
    expect(component.form.getRawValue().title).toBe('Crepes');
    expect(lineCount()).toBe(1);
  });

  it('updates the recipe in edit mode', async () => {
    recipeService.findById.and.returnValue(of(buildRecipe({ id: 7 })));
    recipeService.update.and.returnValue(of(buildRecipe({ id: 7 })));
    create('7');

    await component.submit();

    expect(recipeService.update).toHaveBeenCalledOnceWith(7, buildRecipeRequest());
    expect(recipeService.create).not.toHaveBeenCalled();
  });

  it('shows an error when the recipe to edit does not exist', () => {
    recipeService.findById.and.returnValue(throwError(() => ({ status: 404 })));

    create('7');

    expect(component.error()).toBe('Recette introuvable.');
  });
});
