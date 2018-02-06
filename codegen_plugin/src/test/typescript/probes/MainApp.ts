import {NgModule} from '@angular/core';


import {rootRoutesProvider} from "./Routes";

/**
 * The central applications module
 * which triggers the AppComponent
 * as main page controller component
 */
@NgModule({
    declarations: [],
    imports: [rootRoutesProvider],
    providers: [],
    bootstrap: []
})
export class AppModule {
}
