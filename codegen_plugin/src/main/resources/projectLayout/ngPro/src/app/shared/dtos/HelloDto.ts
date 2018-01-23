/**
 * DTO typescript interface definition for HelloDto
 * this is just for instanceof checks since we cannot inherit
 * between api and impl, and api might rely on foreign base classes
 * So the API is a no go as well.
 *
 * The pattern is if a class inherits this interface, you can
 * savely cast the impl class against the api class although
 * they share only this interface.
 *
 * @ref: com.example.springbootangular2.rest.dto.HelloDto
 */
export interface IHelloDto  {
}

/**
* DTO typescript definition for HelloDto
*
*/
export class HelloDto implements IHelloDto {

    sayHello: string;

}

