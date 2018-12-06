import ITimeoutService = angular.ITimeoutService;
import ICompileService = angular.ICompileService;
import IScope = angular.IScope;
import IPromise = angular.IPromise;
import {AppUtils} from "../../shared/utils/appUtils";

/**
 *
 * Custom bootstrap popup which does more than the standard ng-.... popup
 * we cam embed content or define it as attribute
 *
 * @see http://getbootstrap.com/components/
 */

export var MSG_CLOSE_POPUP = "close.popups";

export function BootstrapPopup($compile: ICompileService, $timeout: ITimeoutService, appUtils: AppUtils) {

    $compile.any("whatever");

    return {
        restrict: 'EA',
        replace: false,
        scope: {
            selector: "@", /*optional selector to to hook the popup to if none is given the parent element is the hook*/
            trigger: "@" /*trigger as defined by @see http://getbootstrap.com/components/*/,
            title: "@", /*optional title also the tag ns-pane-title can be used*/
            content: "@" /*optional content also the tag ns-pane-content can be used*/,
            placement: "@", /*placement as defined by http://getbootstrap.com/components/ */
            appendToBody: "<",//deprecated will be removed use container="body" instead
            container: "@",
            //not supported yet
            template: "@"
        },

        controllerAs: 'ctrl',
        bindToController: true,
        transclude: {
            nsPaneTitle: "?nsPaneTitle",
            nsPaneContent: "?nsPaneContent"
        },
        priority: 10000,

        template: `
          
          <ng-transclude ng-transclude="nsPaneTitle"/>
          <ng-transclude ng-transclude="nsPaneContent">
              <ns-pane-content>  
                    <internal-include />
              </ns-pane-content>   
          </ng-transclude>
        `,


        controller: ["$scope", "$element", "$timeout",
            function ($scope: IScope, $element: JQuery, $timeout: ITimeoutService) {

                var uniqueId = _.uniqueId();

                var hideClickHandler = (event: JQueryEventObject) => {
                    event.preventDefault();
                    if (event.target != this.popover[0]) {
                        this.popover.popover("hide");
                    }
                    angular.element(document.body).unbind("click", hideClickHandler);
                };


                var clickHandlerInside = (event: JQueryEventObject) => {
                    event.stopPropagation();
                };

                var hideDelay: IPromise<any>;

                this.$postLink = function () {


                    var showHandler = (event: JQueryEventObject) => {
                        event.preventDefault();
                        if (hideDelay) {
                            $timeout.cancel(hideDelay);
                        }

                        appUtils.sendEvent(MSG_CLOSE_POPUP, {source: this.popover});

                        if ($element.find("ns-pane-title").length || $element.find($element.find("ns-pane-content")).length) {
                            ($element.find("ns-pane-title").length) ? title = $element.find("ns-pane-title") : null;
                            ($element.find("ns-pane-content").length) ? content = $element.find("ns-pane-content") : null;
                        }

                        //in case of a template we need a separate compile cycle
                        if (!this.content && $element.find("internal-include").length) {
                            ($compile(content))($scope.$parent);
                        }

                        if (realPlacement == "top" || realPlacement == "bottom" || realPlacement == "left" || realPlacement == "right") {
                            _t.popover.popover('show');
                        } else {
                            _t.popover.popover('show');
                            repositionPopup();
                        }
                    };


                    var realPlacement = this.placement;


                    /**
                     * we can close all popups via an outside event
                     */
                    $scope.$on(MSG_CLOSE_POPUP, (event, params) => {
                        if (!params.source || params.source[0] != this.popover[0]) {
                            this.popover.popover("hide");
                            angular.element("#" + uniqueId).remove();
                        }
                    });

                    //We have to compile the children separately
                    //following tags are needed ns-pane-title
                    //ns-pane-content
                    var title: any = null;

                    var content: any = null;

                    this.popover = this.selector ? $(this.selector) : ($element.attr("ns-bootstrap-popup") ? $element : $($element.parent()));


                    var options: any = {};


                    //first we transfer everything into our init params
                    if (this.trigger) {
                        options.trigger = this.trigger;

                    }

                    if (this.container) {
                        options.container = this.container;
                    }
                    if (this.delay) {
                        options.delay = parseInt(this.delay);
                    }
                    if (this.placement) {
                        if (realPlacement == "bottom-left" || realPlacement == "bottom-right") {
                            options.placement = "bottom";
                        }
                        else if (realPlacement == "top-left" || realPlacement == "top-right") {
                            options.placement = "top";
                        } else {
                            options.placement = this.placement;
                        }
                    }

                    if (this.selector) {
                        options.selector = this.selector;
                    }


                    if (!this.title) {
                        options.title = function () {
                            return title;
                        }
                    } else {
                        options.title = this.title;
                    }

                    if (!this.content) {
                        (<any>$scope.$parent)._template = this.template;

                        options.content = () => {
                            return content;
                        }
                    } else {
                        options.content = this.content;
                    }

                    //not sure if this works but we can give it a shot


                    options.html = true;


                    if (!options.trigger) {
                        options.trigger = "custom";
                    } else {
                        options.trigger = this.trigger;
                    }

                    var _t = this;


                    _t.popover.on("mouseenter", () => {

                    });

                    function repositionPopup() {
                        if (_t.container == "body") {
                            _t.appendToBody = true;
                        }

                        var placement = "";
                        if (realPlacement == "bottom-left" || realPlacement == "bottom-right") {
                            placement = "bottom";
                        }
                        else if (realPlacement == "top-left" || realPlacement == "top-right") {
                            placement = "top";
                        } else {
                            placement = _t.placement;
                        }


                        if (realPlacement == "bottom-left" || realPlacement == "top-left") {
                            let offsetTop: number = _t.appendToBody ? _t.popover.offset().top - $(window).scrollTop() : _t.popover.position().top;
                            let offsetLeft: number = _t.appendToBody ? _t.popover.offset().left : $(_t.popover).position().left + 10;
                            $(".popover").css('left', offsetLeft + 'px');
                            $(".popover .arrow").css('left', $(".popover .arrow").outerWidth(true) + 'px');
                        }
                        else if (realPlacement == "bottom-right" || realPlacement == "top-right") {
                            let offsetTop: number = _t.appendToBody ? _t.popover.offset().top - $(window).scrollTop() : $(_t.popover).position().top;
                            let offsetLeft: number = _t.appendToBody ? _t.popover.offset().left + _t.popover.outerWidth(true) : $(_t.popover).position().left - 10;
                            let offsetRight = _t.appendToBody ? $(document.body).outerWidth(true) - offsetLeft : 0;
                            $(".popover").css("left", "auto").css('right', (offsetRight - 10) + 'px');

                            $(".popover .arrow").css('left', 'auto');
                            $(".popover .arrow").css('right', $(".popover .arrow").outerWidth(true) + 'px');
                        }
                        else if (realPlacement == "bottom") {
                            let centerOffset = _t.popover.offset().left + _t.popover.outerWidth() / 2 - $(".popover").outerWidth(true);
                            $(".popover").css("left", centerOffset + "px");

                        }
                    }


                    /*click handling specialized case we also have to close if something is clicked outside*/
                    if (options.trigger == "click") {
                        options.trigger = "custom";
                        //we have to roll our own custom click here
                        //because there was a bug in bootstrap which prevented
                        //the normal click handler to work in conjunction
                        //with outside click closing
                        this.popover.bind("click", (event: JQueryEventObject) => {
                            let popover = _t.popover.attr("aria-describedby");

                            if (!popover || popover.indexOf("popover") == -1) {
                                showHandler(event);
                            } else {
                                this.popover.popover("toggle");
                            }
                        });


                        this.popover.on("shown.bs.popover", () => {
                            $timeout(() => {
                                angular.element(".popover").bind("click", clickHandlerInside);
                                angular.element(document.body).bind("click", hideClickHandler);
                            });
                        });

                        this.popover.on("hidden.bs.popover", () => {
                            angular.element(document.body).unbind("click", hideClickHandler);
                        });

                    } else if (options.trigger == "custom") {
                        var hideDelay: IPromise<any>;

                        var inShow = false;
                        this.popover.on("shown.bs.popover", () => {

                            $timeout(() => {
                                angular.element(".popover").bind("mouseenter", cancelHideHandler);
                                angular.element(".popover").bind("mouseleave", hideHandlerFast);
                                angular.element(".popover").bind("click", clickHandlerInside);
                                angular.element(document.body).bind("click", hideClickHandler);
                            });

                        });

                        this.popover.on("hidden.bs.popover", () => {
                            angular.element(".popover").unbind("mouseenter");
                            angular.element(".popover").unbind("mouseleave");
                            angular.element(document.body).unbind("click", hideClickHandler);
                            this.popover.next(".popover").unbind("click", clickHandlerInside);
                        });

                        var cancelHideHandler = (event: JQueryEventObject) => {
                            event.preventDefault();
                            if (hideDelay) {
                                $timeout.cancel(hideDelay);
                            }
                        };


                        var hideHandler = (event: JQueryEventObject) => {
                            event.preventDefault();
                            if (hideDelay) {
                                $timeout.cancel(hideDelay);
                            }
                            hideDelay = $timeout(() => {
                                this.popover.popover("hide");
                            }, 1000);
                        };

                        var hideHandlerFast = (event: JQueryEventObject) => {
                            event.preventDefault();
                            if (hideDelay) {
                                $timeout.cancel(hideDelay);
                            }
                            hideDelay = $timeout(() => {
                                this.popover.popover("hide");
                            }, 500);
                        };


                        this.popover.bind("mouseenter", showHandler);
                        this.popover.bind("mouseleave", hideHandler);
                    }

                    this.popover.popover(options);
                };

                this.$onDestroy = () => {
                    angular.element(document.body).unbind("click", hideClickHandler);
                    $("#"+this.popover.attr("aria-describedby")).remove();
                    if (this.popover) {
                        //lets destroy any internal bindings
                        this.popover.popover("destroy");
                    }
                };

            }
        ],

        link: ($scope: IScope, $element: JQuery, attributes: any) => {

        }
    }

}

