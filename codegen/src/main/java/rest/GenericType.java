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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import reflector.utils.ReflectUtils;
import reflector.utils.TypescriptTypeMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
/**
 * A generic type definition for our templating engine
 * This class describes a generic type with optional
 * generics embedded
 * aka: java.util.List&lt;myPackage.Fooba&gt;
 * becomes
 * <pre>
 * GenericType {
 *     ownerType: "java.util.List"
 *     childTypes: [{
 *          ownerType: "myPackage.Fooba",
 *          childTypes: []
 *     }]
 * }
 * </pre>
 *
 */
public class GenericType {


    @NonNull
    private final String ownerType;
    @NonNull
    private final List<GenericType> childTypes;



    public String toTypescript(Function<String, String>... reducers) {
        StringBuilder retVal = new StringBuilder();

        String finalOwnerType = Strings.nullToEmpty(ownerType);

        for (Function<String, String> reducer : reducers) {
            finalOwnerType = reducer.apply(finalOwnerType);
        }




        List<String> generics = childTypes.stream().map(inGeneric -> inGeneric.toTypescript(reducers)).collect(Collectors.toList());
        if (!generics.isEmpty()) {
            if (!Strings.isNullOrEmpty(finalOwnerType) && !finalOwnerType.startsWith("Map")) {
                retVal.append(finalOwnerType);
                retVal.append("<");
                retVal.append(Joiner.on(",").join(generics));
                retVal.append(">");
            } else if (!Strings.isNullOrEmpty(finalOwnerType) && finalOwnerType.startsWith("Map") && generics.size() > 1) {
                retVal.append("");
                retVal.append("").append("{[key:")
                        .append(generics.get(0)).append("]:")
                        .append(Joiner.on(",").join(generics.subList(1, generics.size())))
                        .append("}");
            } else if (!Strings.isNullOrEmpty(finalOwnerType) && finalOwnerType.startsWith("Map") && generics.size() < 1) {
                retVal.append("");
                retVal.append("{[")
                        .append(generics.get(0)).append("]:")
                        .append(Joiner.on(",").join(generics.subList(1, generics.size())))
                        .append("}")
                        .append("");
            } else {
                retVal.append(Joiner.on(",").join(generics));
            }

        } else {
            retVal.append(finalOwnerType);
        }
        return retVal.toString();
    }


    public String toTypeScript() {
        String retVal = toTypescript(TypescriptTypeMapper::map, ReflectUtils::reduceClassName);
        if(retVal.startsWith("any<")) {
            return "any";
        }
        return retVal;
    }

    public String getTypeName() {
        return ownerType;
    }

    public boolean hasExtendedType(boolean deep) {
        if(deep && childTypes != null) {
            return childTypes.stream().filter(childType -> {
                return childType.hasExtendedType(true);
            }).findFirst().isPresent() || !ReflectUtils.isJavaType(ownerType);
        }
        return !ReflectUtils.isJavaType(ownerType);
    }

    public boolean hasJavaType(boolean deep) {

        if(deep && childTypes != null && !childTypes.isEmpty() ) {
            return ReflectUtils.isJavaType(ownerType) || childTypes.stream().filter(childType -> {
                return childType.hasJavaType(true);
            }).findFirst().isPresent() ;
        }

        return ReflectUtils.isJavaType(ownerType);
    }

    public List<GenericType> getNonJavaTypes(boolean deep) {
        if(!hasExtendedType(deep)) {
            return Collections.emptyList();
        }
        List<GenericType> retVal = new ArrayList<>();
        if(ownerType != null && !ReflectUtils.isJavaType(ownerType)) {
            retVal.add(this);
        }
        if(deep && childTypes != null && !childTypes.isEmpty()) {
            List<GenericType> flattenedChilds = childTypes.stream().filter(childType -> {
                return childType.hasExtendedType(deep);
            }).flatMap(childType -> childType.getNonJavaTypes(deep).stream())
                    .collect(Collectors.toList());

            retVal.addAll(flattenedChilds);
        }
        return retVal;

    }
}
