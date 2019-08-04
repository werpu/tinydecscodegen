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
                retVal.append("@AString() ");
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
