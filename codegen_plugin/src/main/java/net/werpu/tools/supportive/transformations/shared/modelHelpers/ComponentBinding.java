/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.supportive.transformations.shared.modelHelpers;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class ComponentBinding {
    @EqualsAndHashCode.Exclude
    BindingType bindingType;
    String name;
    @EqualsAndHashCode.Exclude
    String tsType;

    public ComponentBinding(BindingType bindingType, String name) {
        this.bindingType = bindingType;
        this.name = name;
    }

    public String toNgString() {
        StringBuilder retVal = new StringBuilder();

        switch (bindingType) {
            case BOTH: {
                retVal.append("@Output() ");
                retVal.append(name + "Change");
                retVal.append(" = new EventEmitter(); \n\n");
                applyBidirectionalPattern(retVal, true);

                //we need a dedicated setter
                //the pattern is output event emiter
                //@input getter on the value
                //silent setter which sets the value and emits the event
                //this is braindead but oh well

                break;
            }
            case INPUT: {
                retVal.append("@Input() ");
                retVal.append(name);
                retVal.append(": any;");
                break;
            }
            case ASTRING: {
                retVal.append("@AString() ");
                retVal.append(name);
                retVal.append(": string;");
                break;
            }
            case FUNC: {
                retVal.append("@Output() ");
                retVal.append(name);
                retVal.append(" = new EventEmitter();");

                //pattern here the function as eventEmitter
                //callback into the function call from
                // this.<name>({
                //  $param1: value...
                // }}
                // to do something(clickedEntry: SomeModel): void {
                //        this.<name>.emit([param1, ...]);
                //    }

                //TODO the rest of the patterns needs to be fullfilled in the method transformation code section

                break;
            }
            case OPT_BOTH: {
                retVal.append("@Output(true) ");
                retVal.append(name);
                retVal.append("Change = new EventEmitter();");

                applyBidirectionalPattern(retVal, true);

                break;
            }
            case OPT_INPUT: {
                retVal.append("@Input(true) ");
                retVal.append(name);
                retVal.append(": any;");
                break;
            }
            case OPT_FUNC: {
                retVal.append("@Output(true) ");
                retVal.append(name);
                retVal.append(" = new EventEmitter();");
                break;
            }
            case OPT_ASTRING: {
                retVal.append("@Input(true) ");
                retVal.append(name);
                retVal.append(": string;");
                break;
            }
            default:
                break;

        }
        return retVal.toString();
    }

    private void applyBidirectionalPattern(StringBuilder retVal, boolean optional) {
        retVal.append("// <"+ name +"> associated getter setter pattern for bidirectional input output \n ");
        retVal.append(name+"Value: "+ this.tsType+"; \n\n");

        retVal.append("@Input(" + (optional ? "true": "") + ") get "+name+"() {\n");
        retVal.append("    return this."+name+"Value");
        retVal.append("}\n\n");
        retVal.append("set "+ name+"(newValue: "+this.tsType+") {\n");
        retVal.append("    this."+name+"Value = newValue;\n");
        retVal.append("    this."+name+"Change.emit(newValue)\n");
        retVal.append("}\n");
        retVal.append("// </"+ name +"> associated getter setter pattern for bidirectional input output \n ");
    }

    public String toString() {
        StringBuilder retVal = new StringBuilder();

        switch (bindingType) {
            case BOTH: {
                retVal.append("@Both() ");
                retVal.append(name);
                retVal.append(": any;");
                break;
            }
            case INPUT: {
                retVal.append("@Input() ");
                retVal.append(name);
                retVal.append(": any;");
                break;
            }
            case ASTRING: {
                retVal.append("@Input() ");
                retVal.append(name);
                retVal.append(": string;");
                break;
            }
            case FUNC: {
                retVal.append("@Func() ");
                retVal.append(name);
                retVal.append(": Function;");
                break;
            }
            case OPT_BOTH: {
                retVal.append("@Both(true) ");
                retVal.append(name);
                retVal.append(": any;");
                break;
            }
            case OPT_INPUT: {
                retVal.append("@Input() ");
                retVal.append(name);
                retVal.append(": any;");
                break;
            }
            case OPT_FUNC: {
                retVal.append("@Func() ");
                retVal.append(name);
                retVal.append(": Function;");
                break;
            }
            case OPT_ASTRING: {
                retVal.append("@AString() ");
                retVal.append(name);
                retVal.append(": string;");
                break;
            }
            default:
                break;

        }
        return retVal.toString();
    }

}
