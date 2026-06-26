import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class ErrorMappingService {

  constructor(private translate: TranslateService) {}

  /**
   * Extracts the error-message in the correct language from the error
   */
  getErrorMessage(error: any): string {
    const errorBody = error?.error ? error.error : error;
    const currentLang = this.translate.currentLang || this.translate.defaultLang;

    if (currentLang === 'de') {
      let baseDeMessage = errorBody?.deMessage || '';

      if (errorBody?.errors && Array.isArray(errorBody.errors) && errorBody.errors.length > 0) {
        const localizedDetails = errorBody.errors.map((err: any) => {
          if (err && typeof err === 'object') {
            return err.deMessage || err.message;
          }
          return err;
        });

        const joinedErrors = localizedDetails.join(', ');
        return baseDeMessage ? `${baseDeMessage}: ${joinedErrors}` : joinedErrors;
      }

      if (baseDeMessage) {
        return baseDeMessage;
      }
    }

    //Global Fallback: if language isn't de or if de doesn't exist, do the errorMessage in English
    let baseEnMessage = errorBody?.message || '';

    if (errorBody?.errors && Array.isArray(errorBody.errors) && errorBody.errors.length > 0) {
      const localizedDetails = errorBody.errors.map((err: any) => {
        if (err && typeof err === 'object') {
          return err.message || err.deMessage;
        }
        return err;
      });

      const joinedErrors = localizedDetails.join(', ');
      return baseEnMessage ? `${baseEnMessage}: ${joinedErrors}` : joinedErrors;
    }

    if (baseEnMessage) {
      return baseEnMessage;
    }

    //Fallback if Notification contains nothing at all: check whether we actually got the error from server
    if (error.status === 0) {
      return this.translate.instant('COMMON.SERVER_UNREACHABLE')
        || 'The server is currently unreachable. Please check your internet connection and try again later.';
    }

    return this.translate.instant('COMMON.UNEXPECTED_ERROR') || 'An unexpected error occurred.';
  }
}
