import IResourceClass = angular.resource.IResourceClass;
import IResource = angular.resource.IResource;
import IResourceService = angular.resource.IResourceService;
import IPromise = angular.IPromise;
import {TagGlobalSearchParameter, GlobalSearchableEntityType} from "../shared/globalSearchTypes";
import {EmptyMapper} from "../../nsphere/utils/MappingUtils";
import {GlobalSortFilterAttributes} from "../shared/globalFilterAttributes";
import {RestRutils, IRequestPromise} from "../../shared/utils/restRutils";

export class GlobalSearchService{
    static $inject = ["restBasePath", "$resource", "restRutils"];

    private boogaResource: IResourceClass<IResource<any>>;
    private booga2Resource: IResourceClass<IResource<any>>;
    private booga3Resource: IResourceClass<IResource<any>>;

    constructor(private restBasePath:string,
                private $resource:IResourceService,
                private restRutils: RestRutils) {

        this.boogaResource = $resource(restBasePath + "vvvv/booga", {}, {
            'get': {method: "POST", cache: false, isArray: false}
        });
        this.booga2Resource = $resource(restBasePath + "vvvv/booga2", {}, {
            'get': {method: "POST", cache: false, isArray: true}
        });
        this.booga3Resource = $resource(restBasePath + "vvvv/booga3", {}, {
            'get': {method: "POST", cache: false, isArray: true}
        });
    }

    public getSuggestions(searchParams:TagGlobalSearchParameter): IRequestPromise<any>{
        return this.restRutils.wrap(this.boogaResource.get(JSON.stringify(searchParams,EmptyMapper)));
    }
    public getAll(searchParams:TagGlobalSearchParameter): IRequestPromise<any>{
        return this.restRutils.wrap(this.booga2Resource.get(JSON.stringify(searchParams,EmptyMapper)));
    }
    public getResults(searchParams:TagGlobalSearchParameter):IRequestPromise<any>{
        return this.restRutils.wrap(this.booga3Resource.get(JSON.stringify(searchParams,EmptyMapper)));
    }
}