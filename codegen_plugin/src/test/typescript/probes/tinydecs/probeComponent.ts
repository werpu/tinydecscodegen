/**
 * a probe for parsing tiny decs components
 * those need to be transformed into angular components
 *
 */

import IComponentOptions = angular.IComponentOptions;
import IScope = angular.IScope;

import {AString, Both, Component, Func, Inject, Input} from "TinyDecorations";

import {template} from "./templates/ProbeComponent";
import {GlobalSearchOptions} from "../shared/GlobalSearchOptions"
import {GlobalSearchableEntityType} from "../shared/globalSearchTypes";


/**
 * Component probe-component
 * @author ${AUTHOR}
 */
@Component({
    selector: "probe-component",
    template: template,
    controllerAs: "ctrl"
})
export class ProbeComponent {

    @Both() searchOptions: any;
    @Both() suggestions: any;
    @Func() showAllCallback: Function;
    @Func() navigate: Function;
    @Both() showAllSuggestions: any;

    boogaVar: string;


    getClass: Function;

    constructor(@Inject('$scope') private $scope: IScope, @Inject(GlobalSearchOptions) opts: GlobalSearchOptions) {
        var _t = this;

        let myVar2: string = "booga";

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


            let myVar3 = myVar2 ?? "";
            myVar2 = "booga2";
            return myVar2.toString();
        };

        $scope.$watch("ctrl.showAllSuggestions", (newValue: any, oldValue: any) => {
            /*blarg*/
            if(newValue == null) {
                console.log(newValue);
                console.log(oldValue);
            }
        });

        let newVar = () => {
            console.log("scope destroy");
        };
        $scope.$on("$destroy", newVar);

        $scope.$on("$destroy", () => {
            console.log("functionBlock")
        });

        this.blarg();

        if (true == (() => {
            return true
        })()) {
        }

        //this one only has this references
        //which means we can refactor it

    }

    blarg() {
        //prevents refactoring to the outside
        //because if the _t reference
        this.$onInit();
        this.$scope.$watch("ctrl.suggestions", (newValue: string[]) => {
            /*blarg*/
            if(newValue == null) {
                console.log(newValue);
            }
        });
    };

    $onInit() {
        this.GlobalSearchableEntityType = GlobalSearchableEntityType;
    }

    $postLink() {
        alert("booga");
    }

    $onDestroy() {
        console.log("lifecycle hook destroy");
    }

}