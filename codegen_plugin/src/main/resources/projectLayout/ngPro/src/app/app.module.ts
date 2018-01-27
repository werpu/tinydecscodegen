import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {FirstPageModuleModule} from './firstpage/first-page-module/first-page-module.module';
import {moduleWithProviders} from "./app.routes";
import {SharedModule} from "./shared/app.shared.module";

/**
 * The central applications module
 * which triggers the AppComponent
 * as main page controller component
 */
@NgModule({
    declarations: [AppComponent],
    imports: [moduleWithProviders, FirstPageModuleModule, SharedModule],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
