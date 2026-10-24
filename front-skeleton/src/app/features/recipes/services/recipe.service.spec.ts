import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../../environments/environment';
import { Page, Recipe } from '../models/recipe.model';
import { buildPage, buildRecipe, buildRecipeRequest } from '../testing/recipe.fixtures';
import { RecipeService } from './recipe.service';

describe('RecipeService', () => {
  let service: RecipeService;
  let http: HttpTestingController;
  const url = `${environment.apiUrl}/v1/recipes`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(RecipeService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends page, size and the trimmed title when searching', () => {
    service.search('  crepes ', 2, 6).subscribe();

    const req = http.expectOne((r) => r.url === url);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('6');
    expect(req.request.params.get('title')).toBe('crepes');
    req.flush(buildPage([]));
  });

  it('omits the title parameter when the title is blank', () => {
    service.search('   ', 0, 6).subscribe();

    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.has('title')).toBeFalse();
    req.flush(buildPage([]));
  });

  it('returns the page sent by the API', () => {
    const page = buildPage([buildRecipe()]);
    let received: Page<Recipe> | undefined;

    service.search('', 0, 6).subscribe((result) => (received = result));
    http.expectOne((r) => r.url === url).flush(page);

    expect(received).toEqual(page);
  });

  it('gets a recipe by id', () => {
    const recipe = buildRecipe({ id: 5 });
    let received: Recipe | undefined;

    service.findById(5).subscribe((result) => (received = result));

    const req = http.expectOne(`${url}/5`);
    expect(req.request.method).toBe('GET');
    req.flush(recipe);
    expect(received).toEqual(recipe);
  });

  it('posts the request body when creating', () => {
    const body = buildRecipeRequest();
    service.create(body).subscribe();

    const req = http.expectOne(url);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(buildRecipe());
  });

  it('puts the request body when updating', () => {
    const body = buildRecipeRequest({ title: 'Tarte' });
    service.update(3, body).subscribe();

    const req = http.expectOne(`${url}/3`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(body);
    req.flush(buildRecipe({ id: 3, title: 'Tarte' }));
  });

  it('deletes a recipe by id', () => {
    service.delete(4).subscribe();

    const req = http.expectOne(`${url}/4`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
