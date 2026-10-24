import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { RecipeService } from '../services/recipe.service';
import { buildPage, buildRecipe } from '../testing/recipe.fixtures';
import { RecipeListComponent } from './recipe-list.component';

describe('RecipeListComponent', () => {
  let fixture: ComponentFixture<RecipeListComponent>;
  let component: RecipeListComponent;
  let service: jasmine.SpyObj<RecipeService>;

  const recipes = [buildRecipe({ id: 1 }), buildRecipe({ id: 2, title: 'Tarte' })];
  const page = buildPage(recipes, { totalPages: 3, totalElements: 15, last: false });

  beforeEach(() => {
    service = jasmine.createSpyObj<RecipeService>('RecipeService', ['search']);
    service.search.and.returnValue(of(page));

    TestBed.configureTestingModule({
      imports: [RecipeListComponent],
      providers: [{ provide: RecipeService, useValue: service }, provideRouter([])],
    });
    fixture = TestBed.createComponent(RecipeListComponent);
    component = fixture.componentInstance;
  });

  it('loads the first page on init', () => {
    fixture.detectChanges();

    expect(service.search).toHaveBeenCalledOnceWith('', 0, 6);
    expect(component.recipes()).toEqual(recipes);
    expect(component.totalPages()).toBe(3);
    expect(component.totalElements()).toBe(15);
    expect(component.loading()).toBeFalse();
  });

  it('renders one card per recipe', () => {
    fixture.detectChanges();

    const cards = fixture.nativeElement.querySelectorAll('.recipe-card');
    expect(cards.length).toBe(recipes.length);
  });

  it('sets an error message when the API fails', () => {
    service.search.and.returnValue(throwError(() => new Error('fail')));

    fixture.detectChanges();

    expect(component.error()).not.toBeNull();
    expect(component.loading()).toBeFalse();
  });

  it('goes back to the first page when searching', () => {
    fixture.detectChanges();
    component.page.set(2);
    component.title = 'tarte';

    component.onSearch();

    expect(service.search).toHaveBeenCalledWith('tarte', 0, 6);
    expect(component.page()).toBe(0);
  });

  it('loads the requested page', () => {
    fixture.detectChanges();

    component.goTo(1);

    expect(service.search).toHaveBeenCalledWith('', 1, 6);
    expect(component.page()).toBe(1);
  });

  it('ignores a page outside the range', () => {
    fixture.detectChanges();
    service.search.calls.reset();

    component.goTo(-1);
    component.goTo(3);

    expect(service.search).not.toHaveBeenCalled();
  });
});
