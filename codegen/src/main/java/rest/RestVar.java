/*

Copyright 2017 Werner Punz

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software
is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package rest;

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import reflector.utils.ReflectUtils;
import reflector.utils.TypescriptTypeMapper;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Function;


/**
 * A generic rest variable
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class RestVar extends GenericVar {

    private final RestVarType paramType;
    private final boolean array;


    public RestVar(RestVarType paramType, String name, Type classType, boolean array) {
        super(name, classType, new GenericType[0]);
        this.paramType = paramType;
        this.array = array;
    }


    public RestVar(RestVarType paramType, String name, boolean array, Type classType, GenericType... generics) {
        super(name, classType, generics);

        this.paramType = paramType;
        this.array = array;
    }

    @SuppressWarnings("unchecked")
    public String toTypeScript(Function<String, String>... reducers) {

        GenericType varGeneric = new GenericType((classType != null) ? classType.getTypeName() : "", Arrays.asList(generics));
        return (Strings.isNullOrEmpty(name)) ? varGeneric.toTypescript(reducers) : name + ": " +varGeneric.toTypescript(reducers);
    }

    @SuppressWarnings("unchecked")
    public String toTypeScript() {
        return this.toTypeScript(TypescriptTypeMapper::map, ReflectUtils::reduceClassName);
    }

}
