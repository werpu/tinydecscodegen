import {Inject, NgModule} from "TinyDecorations";
import {View2} from "./View2";
import {View2Service} from "./View2Service";

/**
 * Module Module2
 * @author ${AUTHOR}
 */
@NgModule({name: "Module2", providers: [View2Service], exports: [View2]})
export class Module2 {
    static angularModule: any; //classic angular representation of the current module
}


export var app = Module2.angularModule; //add your legacy angular artifacts here