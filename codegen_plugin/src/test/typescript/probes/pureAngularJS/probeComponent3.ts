import IDocumentService = angular.IDocumentService;
import IScope = angular.IScope;
import IComponentOptions = angular.IComponentOptions;
import ITimeoutService = angular.ITimeoutService;
import IAttributes = angular.IAttributes;
import IParseService = angular.IParseService;
import IPromise = angular.IPromise;

export class SuggestionsSearchInput implements IComponentOptions {
    template:any = () => {
        return `
           <form class="app-filters pull-left" role="search">
            <div class="form-group input-group app-filters">
                <input type="text" class="tree-filters" placeholder="{{ctrl.placeholder}}" ng-model="ctrl.searchString" ng-model-options="{debounce: 500}">
                <a ng-click="ctrl._doSearch($event);" href="" style="cursor: pointer;" class="er er-search"><i ></i></a>
                <a ng-click="ctrl.doReset();" href="" class="er er-close"><i ng-click="ctrl.doReset();" ></i></a>
                <button type="submit" class="btn btn-default"  ng-click="ctrl._doSearch2()" style="position: fixed; top: -200px; left: -200px; ;">
                </button>
            </div>
            <div ng-transclude="nsAutoCompleteResults" style="display: none;"></div>
            </form>
        `;
    };

    controllerAs = "ctrl";

    transclude:any = {
        nsAutoCompleteResults: "?nsAutoCompleteResults",

    };

    bindings:any = {
        doSearch: '&',
        onAutoComplete: "&",
        placeholder: '@',
        searchString: '='
    };

    controller:any = ["$scope", "$element", "$document", "$timeout", "$parse",
        function ($scope:IScope, $element:JQuery, $document:IDocumentService, $timeout:ITimeoutService, $parse:IParseService) {

            var _t = this;
            this.doReset = () => {
                this.searchString = "";
                $element.find("input").focus();
            };

            /**
             * helper function to show the popup
             */
            var showPopup = function () {
                $element.find("[ng-transclude=\"nsAutoCompleteResults\"]").css("display", "");
                $element.find("ns-auto-complete-results").css("position", "inherit").show();
                $element.find("ns-auto-complete-results > div").css("top", $element.find(".host").outerHeight(true) + 1);
            };


            /**
             * hides the popup
             */
            var hidePopup = function () {
                $element.find("ns-auto-complete-results").hide();
            };

            /**
             * resize handler responsible for repositioning the popup
             */
            var resizeTimeout: IPromise<any>;
            var windowResizeHandler = () => {
                if(resizeTimeout) {
                    $timeout.cancel(resizeTimeout);
                }
                if($element.find("ns-auto-complete-results:visible").length) {
                    resizeTimeout = $timeout(showPopup, 500);
                }
            };

            /**
             * a focus with an enter afterwards should trigger a search
             */
            var focusHandler = (event: JQueryEventObject) => {
                if(event) {
                    event.preventDefault();
                    event.stopImmediatePropagation();
                    event.stopPropagation();
                }

                $timeout(() => {
                    showPopup();
                });

            };

            var keyUpHandler = (event:JQueryEventObject) => {
                if (event.keyCode == 27 /*escape*/ || event.keyCode == 13 /*enter*/) {
                    //do nothing
                } else {

                }

                if(this.searchString && event.keyCode == 13 ) {
                    $scope.$apply(() => {
                        this.doSearch();
                        hidePopup();
                    });

                    return true;
                }

                if(!$element.find("ns-auto-complete-results:visible").length) {
                    showPopup();
                }
            };

            this.$postLink = () => {
                angular.element(window).resize(windowResizeHandler);

                $timeout(() =>{
                    $element.find(".placeholderRight").click(() => {
                        $element.find("tags-input .tags > input").focus();
                    });
                    $element.find("input").keyup(keyUpHandler).focus(focusHandler);
                    $element.click((event: JQueryEventObject) => {
                        event.stopImmediatePropagation();
                        event.stopPropagation();
                    });



                    angular.element(document).click(() => {
                        hidePopup();
                    });

                });
            };

            this._doSearch = (evt: JQueryEventObject)=>{
                //double click double double search prevention
                //for instance the change listener triggers this method twice

                if(evt) {
                    evt.preventDefault();
                    evt.stopPropagation();
                    evt.stopImmediatePropagation();
                }

                $timeout( () =>{
                    if(this.searchString){
                        $element.find("button[type='submit']").click();
                        hidePopup();
                    }
                },100);
            };

            //I added this function because the keyup handler is not triggered when submitting the form at enter in the smartass IE
            this._doSearch2 = ()=>{
                this.doSearch();
                hidePopup();
            };

            $scope.$watch("ctrl.searchString", (newValue:string, oldValue:string)=>{
                if(newValue && newValue != oldValue && newValue.length > 2){
                    _t.onAutoComplete();
                }
            })


        }];
}