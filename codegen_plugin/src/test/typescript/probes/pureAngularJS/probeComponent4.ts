import IComponentOptions = angular.IComponentOptions;
import {PaginationData} from "../../rim/shared/tableTypes";
import {GlobalSortFilterAttributes} from "../shared/globalFilterAttributes";
import IRootScopeService = angular.IRootScopeService;
import {template} from "./templates/globalResultsTable";
import {GenericMessages} from "../../shared/other/messages";
import {TableColumns, TableColumn} from "../../shared/components/util/tableColumn";
import ITranslateService = angular.translate.ITranslateService;
import ITranslateProvider = angular.translate.ITranslateProvider;
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IFilterService = angular.IFilterService;
import ITimeoutService = angular.ITimeoutService;
import IScope = angular.IScope;
import {GlobalSearchEntityDto} from "../shared/globalSearchTypes"
/**
 * generic control which makes the assingment users list
 * reusable
 * @author Werner Punz
 */
export class GlobalResultsTable implements IComponentOptions {
    controllerAs:string = 'ctrl';

    bindings:any = {
        loadMore: "&",
        navigate: "&"
    };

    template:Function = () => {
        return template;
    };


    controller:any = ["$rootScope", "$translate", "$q", "$timeout", "$scope", function ($rootScope:IRootScopeService, $translate:ITranslateService, $q:IQService, $timeout:ITimeoutService, $scope:IScope) {
        this.results = [];
        this.loading = false;
        this.pagination = new PaginationData(40);
        this.sortFilter = new GlobalSortFilterAttributes();

        this.columnsLeft = new TableColumns(this);
        this.columnsRight = new TableColumns(this);

        this.subtableStyleClassLeft = "col-md-6 col-sm-12";
        this.subtableStyleClassRight = "col-md-6 col-sm-12";
        this.visible = true;


        this._header = {};

        var applyColumn = (columns:TableColumns, key:string, widthXs:number, widthSm:number, widthLg:number):IPromise<any> => {
            var _t=this;
            return $translate(key).then((translation:string) => {
                columns.columns.push(new TableColumn(key, translation, translation,widthXs, widthSm, widthLg));
                this._header[key] = translation;
            }).catch(function () {
                columns.columns.push(new TableColumn(key, key, key, widthXs, widthSm, widthLg));
                _t._header[key] = key;
            });
        };

        $q.all([
            applyColumn(this.columnsLeft, this.sortFilter.NAME, 12, 4, 4),
            applyColumn(this.columnsLeft, this.sortFilter.ID, 12, 4, 4),
            applyColumn(this.columnsLeft, this.sortFilter.TYPE, 12, 4, 4)]).finally(
            () => {
                this.columnsLeft.init();
                console.log(this.columnsLeft);
            }
        );


        $q.all([applyColumn(this.columnsRight, this.sortFilter.ICTO_NO, 12, 4, 4),
            applyColumn(this.columnsRight, this.sortFilter.DESCRIPTION, 12, 7, 7),
            applyColumn(this.columnsRight, '', 12, 1, 1)]).then(
            () => {
                this.columnsRight.init();
            }
        );


        this.refreshTable = () => {
            this.visible = false;
            $timeout(() => {
                this.visible = true;
                $rootScope.$broadcast(GenericMessages.UPDATE_ELLIPSIS, {rootSelector: "#globalForm"});
            })
        };

        this.updateColumns = () => {

            if (!this.columnsLeft.visible) {
                this.subtableStyleClassLeft = " nodisplay ";
                if (!this.columnsRight.visible) {
                    this.subtableStyleClassRight = " nodisplay ";
                } else {
                    this.subtableStyleClassRight = "col-md-12 col-sm-12";
                }
            } else {
                if (!this.columnsRight.visible) {
                    this.subtableStyleClassLeft = "col-md-12 col-sm-12";
                } else {
                    this.subtableStyleClassLeft = "col-md-6 col-sm-12";
                }
            }

            if (!this.columnsRight.visible) {
                this.subtableStyleClassRight = " nodisplay ";
                if (!this.columnsLeft.visible) {
                    this.subtableStyleClassLeft = " nodisplay ";
                } else {
                    this.subtableStyleClassLeft = "col-md-12 col-sm-12";
                }
            } else {
                if (!this.columnsLeft.visible) {
                    this.subtableStyleClassRight = "col-md-12 col-sm-12";
                } else {
                    this.subtableStyleClassRight = "col-md-6 col-sm-12";
                }
            }


            this.refreshTable();
        };

        this.clearFilter = () => {
            this.sortFilter.filterCrit = {};
            this.results = [];
            this.done = false;
            this.doSearch()
        };

        this.doSearch = () => {
            this.pagination = new PaginationData(40);
            this.results = [];
            this.done = false;
            this._loadMore();
        };

        this.orderBy = () => {
            this.pagination = new PaginationData(40);
            this.results = [];
            this.done = false;
            this._loadMore();
        };


        this.applyEntityType= (type:string) => {
            this.sortFilter.filterCrit[this.sortFilter.TYPE] = type
        };

        this._loadMore = () => {
            if (this.done) {
                this.loading = false;
                return;
            }
            if (this.loading) {
                return;
            }
            this.loading = true;
            this.pagination.current += this.pagination.itemsPerPage;

            this.loadMore({current:this.pagination.current, pageSize:this.pagination.itemsPerPage, filter: this.sortFilter}).then((data:Array<GlobalSearchEntityDto>) => {
                this._applyData(data);
            }).catch(() => {
                this.loading = false;
            });
        };

        this._applyData = (data:Array<GlobalSearchEntityDto>) => {
            if (!this.results) {
                this.results = [];
            }
            if (data.length == 0 || data.length < this.pagination.itemsPerPage) {
                this.done = true;
            }

            this.results = this.results.concat(data);
            this.loading = false;
            //update ellipsis automatically called by our end of iterate directive
            $rootScope.$broadcast(GenericMessages.UPDATE_ELLIPSIS, {rootSelector: "#globalForm"})
        };


        this.$onInit = () => {
            this._loadMore();
        }
    }];
}
