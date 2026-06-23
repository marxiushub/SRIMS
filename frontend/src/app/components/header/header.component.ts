import {Component, ElementRef, HostListener, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {TranslateService} from "@ngx-translate/core";
import {AppComponent} from "../../app.component";
import { Collapse } from 'bootstrap';
import {NavbarService} from "../../services/navbar.service";

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss'],
    standalone: false
})
export class HeaderComponent implements OnInit {

  constructor(
    public authService: AuthService,
    public app: AppComponent,
    private translate: TranslateService,
    private navbarService: NavbarService,
    private elementRef: ElementRef) { }

  ngOnInit() {
    this.navbarService.closeNavbar$.subscribe(() => this.closeNavbar());
  }

  get isEnglish(): boolean {
    return this.translate.currentLang === 'en';
  }

  onLanguageToggle(event: Event) {
    const isChecked = (event.target as HTMLInputElement).checked;
    if(isChecked) {
      this.app.switchLanguage('en');
    }
    else{
      this.app.switchLanguage('de');
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const clickedInside = this.elementRef.nativeElement.contains(event.target);

    if (!clickedInside) {
      const navbarEl = document.getElementById('navbarSupportedContent');
      if (navbarEl) {
        const bsCollapse = Collapse.getInstance(navbarEl);
        if (bsCollapse) {
          bsCollapse.hide();
        }
      }
    }
  }

  toggleNavbar(): void {
    const navbarEl = document.getElementById('navbarSupportedContent');
    if (navbarEl) {
      let bsCollapse = Collapse.getInstance(navbarEl);
      if (!bsCollapse) {
        bsCollapse = new Collapse(navbarEl, { toggle: false });
      }
      bsCollapse.toggle();
    }
  }

  closeNavbar(): void {
    const navbarEl = document.getElementById('navbarSupportedContent');
    if (navbarEl) {
      const bsCollapse = Collapse.getInstance(navbarEl);
      if (bsCollapse) {
        bsCollapse.hide();
      }
    }
  }
}
