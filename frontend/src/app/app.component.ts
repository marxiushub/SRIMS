import { Component, LOCALE_ID } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: false
})
export class AppComponent {
  title = 'SE PR Group Phase';

  constructor(private translate: TranslateService) {
    //Fallback-Language if Key for language is missing
    this.translate.setDefaultLang('de');
    //Language that is supposed to be used when first loading the app
    this.translate.use('de');
  }

  //Method to call in HTML to change language during runtime
  switchLanguage(lang: string){
    this.translate.use(lang);
    localStorage.setItem('locale', lang);
  }
}
