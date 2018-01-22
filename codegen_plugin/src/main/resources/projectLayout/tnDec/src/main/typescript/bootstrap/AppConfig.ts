/**
 * Application config definitions
 */
import {Config, Inject} from "TinyDecorations";
import {ILocationProvider} from "angular";

@Config()
export class AppConfig {
    constructor(@Inject("$locationProvider") private $locationProvider: ILocationProvider,
                @Inject("$routeProvider") private $routeProvider: any) {
        $locationProvider.hashPrefix('!');
        $routeProvider.otherwise({redirectTo: '/view1'});
        console.log("config called");
    }
}
