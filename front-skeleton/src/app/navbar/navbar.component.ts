import { Component, inject } from "@angular/core"
import { takeUntilDestroyed } from "@angular/core/rxjs-interop"
import { NavigationEnd, Router } from "@angular/router"
import { filter } from "rxjs"
import { Link } from "models/links.model"

@Component({
  selector: "navbar",
  templateUrl: "./navbar.component.html",
  styleUrls: ["./navbar.component.scss"],
})
export class NavbarComponent {
  private readonly router = inject(Router)

  links: Link[] = []
  isHome = true

  constructor() {
    this.links.push({ name: "Recettes", href: "recettes" })

    this.isHome = this.isHomeUrl(this.router.url)
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed()
      )
      .subscribe((event) => (this.isHome = this.isHomeUrl(event.urlAfterRedirects)))
  }

  private isHomeUrl(url: string): boolean {
    const path = url.split("?")[0]
    return path === "/" || path === "" || path === "/recettes"
  }
}
