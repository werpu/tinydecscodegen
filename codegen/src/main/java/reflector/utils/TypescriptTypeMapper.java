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
package reflector.utils;

/**
 * A helper class to my various java datatypes into
 * their typescript types
 */
public class TypescriptTypeMapper {

    public static String map(String in) {
        in = in.trim();
        if (in.equals("java.lang.String") || in.equals("String")) {
            return "string";
        } else if (in.equals("int") || in.equals("long") || in.equals("Integer") || in.equals("Long") || in.equals("Numeric") || in.equals("java.lang.Integer") || in.equals("java.lang.Long") | in.equals("java.lang.Numeric")) {
            return "number";
        } else if (in.equals("float") || in.equals("double") || in.equals("Float") || in.equals("Double") || in.equals("java.lang.Float") || in.equals("java.lang.Double")) {
            return "float";
        } else if (in.equals("boolean") || in.equals("Boolean") || in.equals("java.lang.Boolean")) {
            return "boolean";
        } else if (in.equals("java.util.List") || in.equals("List") || in.equals("Set") || in.equals("java.util.Set")) {
            return "Array";
        } else if (in.equals("Class") || in.equals("java.lang.Class")){
            return "any";
        } else if (in.equals("Object") || in.equals("java.lang.Object")){
            return "any";
        } else {
            return ReflectUtils.reduceClassName(in);
        }
    }
}
