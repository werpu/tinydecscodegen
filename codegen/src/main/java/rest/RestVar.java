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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * A generic rest variable
 */

@EqualsAndHashCode(callSuper = true)
public class RestVar extends GenericVar {

    @Getter
    private final RestVarType paramType;
    @Getter
    private final boolean array;

    private final String restName;


    public RestVar(RestVarType paramType, String restName,  String name, GenericType classType, boolean array) {
        super(name, classType, new GenericType[0]);
        this.paramType = paramType;
        this.array = array;
        this.restName = restName;
    }


    public RestVar(RestVarType paramType, String restName, String name, boolean array, GenericType classType, GenericType... generics) {
        super(name, classType, generics);

        this.paramType = paramType;
        this.array = array;
        this.restName = restName;
    }

    public String getRestName() {
        return Strings.isNullOrEmpty(restName) ? name: restName;
    }

    @SuppressWarnings("unchecked")
    public String toTypeScript(Function<String, String>... reducers) {

        GenericType varGeneric = new GenericType((classType != null) ? classType.getTypeName() : "", Arrays.asList(generics));
        return (Strings.isNullOrEmpty(name)) ? varGeneric.toTypescript(reducers) : name + ": " +varGeneric.toTypescript(reducers);
    }

    @SuppressWarnings("unchecked")
    public String toTypeScript() {
        String retVal = this.toTypeScript(TypescriptTypeMapper::map, ReflectUtils::reduceClassName);
        if(retVal.startsWith("any<")) {
            return "any";
        }
        return retVal;
    }


    public List<GenericType> getNonJavaTypes(boolean deep) {
            List<GenericType> retVal = new ArrayList<>();

            if(this.getClassType() != null) {
                retVal.addAll(getClassType().getNonJavaTypes(deep));

            }
            if(deep && generics != null) {
                List found =
                        Arrays.stream(generics).flatMap(generic -> {
                            return generic.getNonJavaTypes(deep).stream();
                        }).collect(Collectors.toList());
                retVal.addAll(found);
            }

            return retVal;
    }

}
