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

            let myVar2:string = "booga";

            //not refactorable to the outside
            //because of its references into myvar2
            this.getClass = () => {
                let columnsNo = this.searchOptions.columnsNo;

                if (columnsNo < 5) {
                    return "aa-p";
                }
                if (columnsNo == 5) {
                    return "bb-p";
                }
                if (columnsNo == 6) {
                    return "cc-p";
                } else {
                    myVar2 = "vv-p";
                }

                //because of this assignment
                //an externalisation is not possible
                String myVar3 = Strings.nullToEmpty(myVar2);
                myVar2 = "booga2";
                return myVar2.toString();
            };

            this.blarg = function() {
                //prevents refactoring to the outside
                //because if the _t reference
                _t.$onInit();
            };


            this.blarg();

            if(true == (() => {return true})()) {
            }

            //this one only has this references
            //which means we can refactor it
            this.$onInit = () => {
                this.GlobalSearchableEntityType = GlobalSearchableEntityType;
            }
        }];

    template: Function = () => {
        return template;
    }
}