import IPromise = angular.IPromise;
import {Injectable, extended} from "TinyDecorations";
import Rest = extended.Rest
import Restable = extended.Restable;
import PathVariable = extended.PathVariable;
import RequestBody = extended.RequestBody;
import RequestParam = extended.RequestParam;

/**
 * Rest service ${service.serviceName}
 * @ref: ${service.ref}
 */

@Injectable("${service.serviceName}")
<#if service.serviceRootUrl?has_content>
@Restable({
    $rootUrl: "${service.serviceRootUrl}"
})
</#if>
export class ${service.serviceName} {

    constructor() {}

    <#list service.methods as method>

    ${method.comment}
    @Rest({
        url: "${method.url}",
        method: "${method.restType.name()}"<#if method.returnValue.get().array == true >,
        isArray:  true</#if>
    })
    ${method.name} (
        <#list method.params as param>@${param.paramType.name()}(<#if param.paramType.requestParam>"${param.restName}"</#if>) ${param.toTypeScript()}<#sep>,
        </#sep></#list>): IPromise<<#if !method.returnValue.isPresent()>any<#else>${method.returnValue.get().toTypeScript()}></#if> {
        return null;
    }
    </#list>
}