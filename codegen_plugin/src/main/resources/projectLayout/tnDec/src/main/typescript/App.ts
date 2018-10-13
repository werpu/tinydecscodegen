/// <reference path="${proj_root_rel}/node_modules/ts-ng-tinydecorations/dist/TinyDecorations.d.ts" />
/// <reference path="${proj_root_rel}/node_modules/ts-ng-tinydecorations/dist/Dto.d.ts" />
/// <reference path="${proj_root_rel}/node_modules/ts-ng-tinydecorations/dist/Cache.d.ts" />
/// <reference path="${proj_root_rel}/node_modules/ts-ng-tinydecorations/dist/Routing.d.ts" />


import {Module2} from "./module2/Module2";
import {Module1} from "./module1/Module1";
import {RouteConfig} from "./bootstrap/Routes";
import {AppRun} from "./bootstrap/AppRun";
import {AppConfig} from "./bootstrap/AppConfig";

import {NgModule, platformBrowserDynamic} from "TinyDecorations";

@NgModule({
    name: "myApp",
    declarations: [AppConfig, AppRun, RouteConfig],
    imports: ["ngRoute", "ngResource", Module2, Module1,"ui.router"]
})
export class MyApp {
}


/*now lets bootstrap the application, unfortunately ng-app does not work due to the systemjs lazy binding*/
platformBrowserDynamic().bootstrapModule(MyApp);
