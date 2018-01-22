import {Inject, NgModule} from "TinyDecorations";
import {View1} from "./View1";

/**
 * Module Module1
 * @author ${AUTHOR}
 */
@NgModule({name: "Module1", exports: [View1]})
export class Module1 {
   static angularModule: any; //classic angular representation of the current module
}


export var app = Module1.angularModule; //add your legacy angular artifacts here