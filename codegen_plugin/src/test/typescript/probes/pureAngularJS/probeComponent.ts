import IComponentOptions = angular.IComponentOptions;
import IScope = angular.IScope;
import {template} from "./templates/ProbeComponent";
import {GlobalSearchOptions} from "../shared/GlobalSearchOptions"
import {GlobalSearchableEntityType} from "../shared/globalSearchTypes";


export class ProbeComponent implements IComponentOptions {
    bindings: any = {
        searchOptions: '=',
        suggestions: '=',
        showAllCallback: '&',
        navigate: '&',
        showAllSuggestions: '='
    };
    controllerAs: string = "ctrl";

    controller: any = ["$scope",
        function ($scope: IScope) {
            var _t = this;

            this.getClass = () => {
                var columnsNo = this.searchOptions.columnsNo;
                if (columnsNo < 5) {
                    return "aa-p";
                }
                if (columnsNo == 5) {
                    return "bb-p";
                }
                if (columnsNo == 6) {
                    return "cc-p";
                } else {
                    return "vv-p";
                }
            };

            this.blarg = function() {
                //
                _t.$onInit();
            };


            this.blarg();

            if(true == (() => {return true})()) {

            }


            this.$onInit = () => {
                this.GlobalSearchableEntityType = GlobalSearchableEntityType;
            }
        }];

    template: Function = () => {
        return template;
    }
}