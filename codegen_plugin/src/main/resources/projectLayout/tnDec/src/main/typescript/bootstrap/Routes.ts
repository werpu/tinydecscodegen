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

/**
 * Define your routes here
 */
import {View1} from "../module1/View1";
import {Config, Inject} from "TinyDecorations";
import {MetaData} from "Routing";
import {View2} from "../module2/View2";
import {StateProvider} from "@uirouter/angularjs";

/**
 * @meta: rootRouteConfig
 *
 * do not delete the meta flag because this is a fallback if
 * we cannot detect the root route config fully
 */
@Config()
export class RouteConfig {
    constructor(@Inject("$stateProvider") private $stateProvider: StateProvider) {
        $stateProvider.state("default", MetaData.routeData(View1, {
            url: "/"
        }));
        $stateProvider.state("view1", MetaData.routeData(View1, {
            url: "/view1"
        }));
        $stateProvider.state("view2", MetaData.routeData(View2, {
            url: "/view2"
        }));
    }
}

