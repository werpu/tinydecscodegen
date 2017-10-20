/**
* Typescript 1.8 string enums
* see http://stackoverflow.com/questions/15490560/create-an-enum-with-string-values-in-typescript
*
* This works with json conversion and theoretically should work both ways
*
* @ref: ${clazz.clazz.ownerType}
*/
export type ${clazz.name} =
<#list clazz.attributes as attr>
    "${attr}"<#sep> |
</#sep></#list>;

export const ${clazz.name} = {
<#list clazz.attributes as attr>
${attr}: "${attr}" as ${clazz.name}<#sep>,
</#sep></#list>

};