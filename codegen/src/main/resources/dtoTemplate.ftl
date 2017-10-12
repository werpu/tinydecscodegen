//dto is a submodule of the tiny decorations project
import {Dto, PostConstruct} from "Dto";

/**
 * DTO typescript interface definition for ${clazz.name}
 *
 * @ref: ${clazz.clazz.ownerType}
 */
export interface I${clazz.name} <#if clazz.parentClass??> extends I${clazz.parentClass.name}</#if> {

<#list clazz.properties as prop>
${prop.name}: ${prop.classType.toTypeScript()};
</#list>

}

/**
* DTO typescript definition for ${clazz.name}
*
* @ref: ${clazz.clazz.ownerType}
*/
export class ${clazz.name}<#if clazz.parentClass??> extends ${clazz.parentClass.name}</#if> implements I${clazz.name} {

<#list clazz.properties as prop>
    ${prop.name}: ${prop.classType.toTypeScript()};
</#list>

}

/**
* Implementation class for for ${clazz.name}
*/
@Dto({<#list clazz.getNonJavaProperties(true)! as prop>
        ${prop.name}: ${prop.nonJavaTypesToString(true)!}Impl<#sep>,
</#sep></#list>
})
export class ${clazz.name}Impl <#if clazz.parentClass??> extends ${clazz.parentClass.name}Impl</#if> implements I${clazz.name} {
    constructor(data: I${clazz.name}) {<#if clazz.parentClass??>

        super(data);</#if>
    }

    @PostConstruct()
    postConstruct(data: ${clazz.name}) {
        //fill in your post init code here, the data is not at an applied
        //stage in the constructor
    }

    //you can add custom behavior here

}