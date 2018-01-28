import {APP_INITIALIZER, NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {FirstPageModuleModule} from './firstpage/first-page-module/first-page-module.module';
import {rootRoutesProvider} from "./app.routes";
import {SharedModule} from "./shared/app.shared.module";
import {OnAppInit} from "./app.init";


/**
 * The central applications module
 * which triggers the AppComponent
 * as main page controller component
 */
@NgModule({
  declarations: [AppComponent],
  imports: [rootRoutesProvider, FirstPageModuleModule, SharedModule],
  providers: [{
    provide: APP_INITIALIZER,
    useFactory: OnAppInit,
    multi: true,
    deps: [/*dependencies like services used by the init function must!!! be declared here*/]
  }],
  bootstrap: [AppComponent]
})
export class AppModule {
}
