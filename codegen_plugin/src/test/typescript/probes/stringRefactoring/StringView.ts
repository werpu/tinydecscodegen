import {Controller, Inject} from "TinyDecorations";
import IScope = angular.IScope;

/**
 * Controller View1
 * @author ${AUTHOR}
 */
@Controller({
    name: "View1",
    template: `<h1>Hello from View1</h1>
<p translate="case1 from me">test</p>
<p>case2</p>
<p>{{'case 3'}}</p>
<p>{{'case 4' |translate}}</p>`,
    controllerAs: "ctrl"
})
export class StringView {

    constructor(@Inject("$scope") private $scope: IScope) { //add your injections here
        //add your constructor code here
    }

}
