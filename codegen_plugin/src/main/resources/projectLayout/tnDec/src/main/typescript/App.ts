/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

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
