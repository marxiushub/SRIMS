import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {TranslateService} from "@ngx-translate/core";
import {AppComponent} from "../../app.component";

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
    private translate: TranslateService,) { }

  ngOnInit() {
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

}
