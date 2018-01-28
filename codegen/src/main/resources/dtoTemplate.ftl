import {Dto, PostConstruct} from "Dto";

/**
 * DTO typescript interface definition for ${clazz.name}
 * this is just for instanceof checks since we cannot inherit
 * between api and impl, and api might rely on foreign base classes
 * So the API is a no go as well.
 *
 * The pattern is if a class inherits this interface, you can
 * savely cast the impl class against the api class although
 * they share only this interface.
 *
 * @ref: ${clazz.clazz.ownerType}
 */
export interface I${clazz.name}  {
}

/**
* DTO typescript definition for ${clazz.name}
*
*/
export class ${clazz.name}<#if clazz.parentClass??> extends ${clazz.parentClass.name}</#if> implements I${clazz.name} {

<#list clazz.properties as prop>
    ${prop.name}: ${prop.classType.toTypeScript()};
</#list>

}

/**
* Implementation class for for ${clazz.name}
*/
@Dto({/* enable what you want to have mapped
<#list clazz.getNonJavaProperties(true)! as prop>
        ${prop.name}: ${prop.nonJavaTypesToString(true)!}Impl<#sep>,
</#sep></#list>
*/})
export class ${clazz.name}Impl <#if clazz.parentClass??> extends ${clazz.parentClass.name}Impl</#if> implements I${clazz.name} {
    constructor(data: ${clazz.name}) {<#if clazz.parentClass??>

        super(data);</#if>
    }

    @PostConstruct()
    postConstruct(data: ${clazz.name}) {
        //fill in your post init code here, the data is
        //already applied to this, but you can
        //alter it on the fly
    }

    //you can add custom behavior here

}