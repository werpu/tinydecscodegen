/**
 * Application config definitions
 */
import {Config, Inject} from "TinyDecorations";
import {ILocationProvider} from "angular";

@Config()
export class AppConfig {
    constructor(@Inject("$locationProvider") private $locationProvider: ILocationProvider) {
        $locationProvider.hashPrefix('!');
        //place your application config here
    }
}
