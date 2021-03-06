/**
 * Define your routes here
 */
import {View1} from "./module1/View1";
import {Config, Inject} from "TinyDecorations";
import {MetaData} from "Routing";
import {View2, View2 as xxx} from "./module2/View2";


/**
 * @meta: rootRouteConfig
 *
 * do not delete the meta flag because this is a fallback if
 * we cannot detect the root route config fully
 */
@Config()
export class RouteConfig {
    constructor(@Inject("$routeProvider") private $routeProvider: any) {
        $routeProvider.otherwise({redirectTo: '/view1'});
        $routeProvider.when("/view1", MetaData.routeData(View1));

        //Special case, chained whens over a builder pattern<
        $routeProvider.when("/view2", MetaData.routeData(View2))
            .when("/view3", MetaData.routeData(xxx)); //remapped view2, also another special case of variable resolutioin
    }
}

