import {NgModule} from '@angular/core';
import {FirstPageComponent} from './pages/first-page/first-page.component';
import {MainPageComponent} from './pages/main-page/main-page.component';
import {SecondPageComponent} from './pages/second-page/second-page.component';

import {RestServiceService} from './services/rest-service.service';
import {localRoutesProvider} from "../../app.routes";
import {SharedModule} from "../../shared/app.shared.module";
import {MyService} from "./services/MyService";


@NgModule({
  declarations: [FirstPageComponent, SecondPageComponent, MainPageComponent],
  imports: [SharedModule, localRoutesProvider(/*add any locally scoped routes here*/)],
  exports: [],
  providers: [RestServiceService, MyService]
})
export class FirstPageModuleModule {
}
