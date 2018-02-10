import { Injectable } from '@angular/core';
import { HttpParams, HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Rx';

/**
 * Rest service ${service.serviceName}
 * @ref: ${service.ref}
 */

@Injectable()
export class ${service.serviceName} {

<#if service.serviceRootUrl?has_content>
    restRoot: string = "${service.serviceRootUrl}";
<#else>
    restRoot: string = "";// fill in your rest request root here
</#if>

    constructor(private http: HttpClient) {
    }
<#list service.methods as method>

    ${method.name} (<#assign mType  = "${method.restType.name()?lower_case}"><#list method.params as param>${param.toTypeScript()}<#sep>,
    </#sep></#list>): Observable<<#if !method.returnValue.isPresent() || method.returnValue.get().toTypeScript() == 'void'>any<#else>${method.returnValue.get().toTypeScript()}></#if>> {

        let params:HttpParams = new HttpParams();

    <#list method.params as param>
        <#if mType = "post" || mType = "put"|| mType = "patch">
        let body: string = null;
            <#if param.paramType.name() = "RequestBody">
        body = JSON.stringify(${param.restName});
            <#else>
        params = params.append('${param.restName}', ${param.restName});
            </#if>
        <#else>
            <#if param.paramType.name() != "RequestBody">
        params = params.append('${param.restName}', ${param.restName});
            </#if>
        </#if>
    </#list>

    <#if mType = "post" || mType = "put"|| mType = "patch">
        let retVal = this.http.${mType?lower_case}(this.restRoot + "<#if service.serviceRootUrl?has_content></#if>${method.url}<#list method.params as param><#if param.paramType.name() = "PathVariable">/<#noparse>${</#noparse>${param.restName}<#noparse>}</#noparse></#if></#list>", body, {
            params: params
        });
    <#else>
        let retVal =  this.http.${mType?lower_case}(this.restRoot + "<#if service.serviceRootUrl?has_content></#if>${method.url}<#list method.params as param><#if param.paramType.name() = "PathVariable">/<#noparse>${</#noparse>${param.restName}<#noparse>}</#noparse></#if></#list>", {
            params: params
        });
    </#if>
        return <Observable<<#if !method.returnValue.isPresent() || method.returnValue.get().toTypeScript() == 'void'>any<#else>${method.returnValue.get().toTypeScript()}></#if>>> retVal;
    }
</#list>

}