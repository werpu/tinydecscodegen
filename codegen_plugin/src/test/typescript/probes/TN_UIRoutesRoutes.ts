/**
 * Define your routes here
 */
import {View1} from "../module1/View1";
import {Config, Inject} from "TinyDecorations";
import {MetaData} from "Routing";
import {View2} from "../module2/View2";

/**
 * @meta: rootRouteConfig
 *
 * do not delete the meta flag because this is a fallback if
 * we cannot detect the root route config fully
 */
@Config()
export class RouteConfig {
    constructor(@Inject("$stateProvider") private $stateProvider: any) {
        $stateProvider.state(MetaData.routeData(View1 ,{
            url: "/view1",
            name: "myName1"
        }))
            .state(MetaData.routeData(View2, {
                url: "/view2",
                name: "myName2"
            }));
    }
}

