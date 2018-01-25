import { Injectable } from '@angular/core';
import { HttpParams, HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Rx';
import { User } from "app/models/user";

/**
 * Rest service ${service.serviceName}
 * @ref: ${service.ref}
 */

@Injectable()
export class ${service.serviceName} {

    constructor(private http: HttpClient) {
    }



<#list service.methods as method>

    ${method.name} (
    <#assign mType  = "${method.restType.name()?lower_case}">
    <#list method.params as param>${param.toTypeScript()}<#sep>,
    </#sep></#list>): Observable<<#if !method.returnValue.isPresent()>any<#else>${method.returnValue.get().toTypeScript()}></#if> {

        let params:HttpParams = new HttpParams();
        let body: string = null;
    <#list method.params as param>

        <#if param.paramType.name() = "RequestBody">
        body = JSON.stringify(${param.restName});
        <#else>
         params = params.append('${param.restName}', ${param.restName});
        </#if>
    </#list>
    <#if mType = "post" || mType = "put"|| mType = "patch">
        return this.http.${mType?lower_case}("<#if service.serviceRootUrl?has_content>${service.serviceRootUrl}</#if>${method.url}<#list method.params as param><#if param.paramType.name() = "PathVariable">/<#noparse>${</#noparse>${param.restName}<#noparse>}</#noparse></#if></#list>", body, {
            params: params
        });
    <#else>
        return this.http.${mType?lower_case}("<#if service.serviceRootUrl?has_content>${service.serviceRootUrl}</#if>${method.url}<#list method.params as param><#if param.paramType.name() = "PathVariable">/<#noparse>${</#noparse>${param.restName}<#noparse>}</#noparse></#if></#list>", {
            params: params
        });
    </#if>
    }
</#list>

}