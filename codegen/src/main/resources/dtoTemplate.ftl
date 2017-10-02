/**
* DTO typescript definition for ${clazz.name}
*
* @ref: ${service.className}
*/
export class ${clazz.name}<#if clazz.parentClass??> extends ${clazz.parentClass.name}</#if> {
<#list clazz.properties as prop>

    ${prop.name}: ${prop.classType.toTypeScript()};
</#list>

}