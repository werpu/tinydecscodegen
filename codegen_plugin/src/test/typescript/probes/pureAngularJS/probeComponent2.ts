import IComponentOptions = angular.IComponentOptions;
import {template} from "./templates/suggestionPanel";
import IScope = angular.IScope;
import {GlobalSearchOptions} from "../shared/GlobalSearchOptions"
import {GlobalSearchableEntityType} from "../shared/globalSearchTypes";



export class SuggestionPanel implements IComponentOptions{
    bindings:any = {
        searchOptions: '=',
        suggestions: '=',
        showAllCallback: '&',
        navigate: '&',
        showAllSuggestions: '='
    };

    controllerAs:string = "ctrl2";

    booga = 1;

    controller:any = ["$scope",
        function ($scope:IScope){
            var _t = this;

            this.getClass = () => {
                var columnsNo = this.searchOptions.columnsNo;
                if(columnsNo < 5){
                    return  "col-24-p";
                }if(columnsNo == 5){
                    return "col-19-p";
                }if(columnsNo == 6){
                    return "col-16-p";
                }else{
                    return "col-14-p";
                }
            };


            this.$onInit = () =>{
                this.GlobalSearchableEntityType = GlobalSearchableEntityType;
            }
        }];

    template:Function = () => {
        return "hello world";
    }
}