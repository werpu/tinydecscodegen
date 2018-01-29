import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {FirstPageModuleModule} from './firstpage/first-page-module/first-page-module.module';
import {rootRoutesProvider} from "./app.routes";
import {SharedModule} from "./shared/app.shared.module";
import {AppInitializerService} from "./app.init";
import {HttpInterceptorService} from "./app.http";

/**
 * The central applications module
 * which triggers the AppComponent
 * as main page controller component
 */
@NgModule({
  declarations: [AppComponent],
  imports: [rootRoutesProvider, FirstPageModuleModule, SharedModule],
  providers: [AppInitializerService, HttpInterceptorService],
  bootstrap: [AppComponent]
})
export class AppModule {
}
