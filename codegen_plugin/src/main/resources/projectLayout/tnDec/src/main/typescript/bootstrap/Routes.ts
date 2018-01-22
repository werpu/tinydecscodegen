/**
 * Define your routes here
 */
import {View1} from "../module1/View1";
import {Config, Inject} from "TinyDecorations";
import {MetaData} from "Routing";
import {View2} from "../module2/View2";

@Config()
export class RouteConfig {
    constructor(@Inject("$routeProvider") private $routeProvider: any) {
        $routeProvider.when("/view1", MetaData.routeData(View1));
        $routeProvider.when("/view2", MetaData.routeData(View2));
    }
}

