import {Controller, Inject} from "TinyDecorations";
import IScope = angular.IScope;

/**
 * Controller View1
 * @author ${AUTHOR}
 */
@Controller({
    name: "View1",
    template: `<h1>Hello from view1</h1>`,
    controllerAs: "ctrl"
})
export class View1 {

    constructor(@Inject("$scope") private $scope: IScope) { //add your injections here
        //add your constructor code here
    }
    
}