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
package rest;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import reflector.utils.ReflectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
/**
 * generic variable
 */
public class GenericVar {
    protected final String name;
    protected final GenericType classType;
    protected final GenericType[] generics; //TODO probably deprecated since the classtype itself can handle the generics

    public boolean hasExtendedType(boolean deep) {
        boolean hasIt = false;
        if(classType != null) {
            hasIt = classType.hasExtendedType(deep);
        }
        return hasIt || Arrays.stream(generics).filter(generic -> generic.hasExtendedType(deep)).findFirst().isPresent();
    }

    public List<GenericType> getNonJavaTypes(boolean deep) {
        List<GenericType> retVal = new ArrayList<>();
        if(classType != null) {
            retVal.addAll(classType.getNonJavaTypes(deep));
        }

        retVal.addAll(Arrays.stream(generics).flatMap(generic -> generic.getNonJavaTypes(deep).stream()).collect(Collectors.toList()));
        return retVal;
    }

    public String nonJavaTypesToString(boolean deep) {
        Optional<String> reduction = getNonJavaTypes(deep).stream().map(item -> item.getTypeName()).map(ReflectUtils::reduceClassName).reduce((oldStr, newStr) -> oldStr+", "+newStr);
        if(reduction.isPresent()) {
            return reduction.get();
        }
        return "any";
    }
}
