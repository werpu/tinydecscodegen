import {Controller, Inject} from "TinyDecorations";
import IScope = angular.IScope;

/**
 * Controller View2
 * @author ${AUTHOR}
 */
@Controller({
    name: "View2",
    template: `\`<p>This is the partial for view 2.</p>`,
    controllerAs: "ctrl"
})
export class View2 {

    constructor(@Inject("$scope") private $scope: IScope) { //add your injections here
        //add your constructor code here
    }
    
}