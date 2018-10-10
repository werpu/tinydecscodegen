import {Inject} from "TinyDecorations";
import {Config, MetaData} from "TinyDecorations";
import {View1} from "./module1/View1";
import {View2} from "./module2/View2";

@Config()
export class RouteConfig {
    constructor(@Inject("$stateProvider") private $stateProvider: any) {
        $stateProvider.state(
            MetaData.routeData(View1,
                {

                    url: "/myState"
                }
            )
        ).state("myState.state2",
            MetaData.routeData(View2,
                {
                    url: "/myState"
                }
            )
        ).state(
            MetaData.routeData(View2,
                {
                    name: "myState.state4x",
                    url: "/myState"
                }
            )
        ).state("mystate1", {
            url: "bloobal",
            views: {
                "viewMain": MetaData.routeData(View2,
                    {
                        name: "myState.state4",
                        url: "/myState"
                    }
                ),
                "viewsecondary": MetaData.routeData(View2,
                    {
                        name: "myState.state5",
                        url: "/myState"
                    }
                )
            }
        });
        $stateProvider.state("mystate2",
            MetaData.routeData(View2,
                {
                    name: "myState.state4y",
                    url: "/myState"
                }
            ));

    }
}