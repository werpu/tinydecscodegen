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
package utils;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import reflector.utils.ReflectUtils;
import rest.GenericClass;
import rest.GenericType;
import rest.GenericVar;

import java.util.*;
import java.util.stream.Collectors;

public class IntellijSpringJavaRestReflector {


    public static List<GenericClass> reflectDto(List<PsiClass> toReflect, String includingEndpoint) {

        return toReflect.stream().filter(clazz -> clazz.hasModifierProperty(PsiModifier.PUBLIC)).map(clazz -> {
            GenericClass parent = null;
            Collection<GenericVar> props = getAllProperties(clazz);
            if (includingEndpoint.equals(clazz.getQualifiedName()) && clazz.getSuperClass() != null && !clazz.getSuperClass().getQualifiedName().equals(Object.class.getName())) {
                parent = reflectDto(Arrays.asList(clazz.getSuperClass()), includingEndpoint).get(0);
            }
            GenericType classDescriptor = ReflectUtils.buildGenericTypes(clazz.getQualifiedName()).get(0);

            return new GenericClass(classDescriptor, parent, Collections.emptyList(), props.stream().collect(Collectors.toList()));

        }).collect(Collectors.toList());
    }

    public static Collection<GenericVar> getAllProperties(PsiClass clazz) {

        PsiField[] fields = clazz.getAllFields();

        PsiMethod[] methods = clazz.getAllMethods();


        List<GenericVar> retVal = Lists.newArrayListWithCapacity(30);
        retVal.addAll(resolveGetters(Collections.emptyList(), methods));
        retVal.addAll(resolveLombok(retVal, fields));
        retVal.addAll(resolvePublicProps(retVal, fields));
        return retVal.stream()
                .sorted(Comparator.comparing(GenericVar::getName))
                .collect(Collectors.toList());

    }

    private static List<GenericVar> resolvePublicProps(List<GenericVar> before, PsiField[] fields) {
        final Set<String> propIdx = before.parallelStream().map(prop -> {
            return prop.getName();
        }).collect(Collectors.toSet());

        return Arrays.asList(fields).stream()
                .filter(declaredField -> {
                    return !propIdx.contains(declaredField.getName()) && declaredField.hasModifierProperty(PsiModifier.PUBLIC)
                            && !declaredField.hasModifierProperty(PsiModifier.STATIC);
                }).map(declaredField -> {
                    List<GenericType> genericTypes = ReflectUtils.buildGenericTypes(declaredField.getType().getCanonicalText());
                    return new GenericVar(declaredField.getName(), genericTypes.get(0), new GenericType[0]);
                }).collect(Collectors.toList());
    }

    private static List<GenericVar> resolveLombok(List<GenericVar> before, PsiField[] fields) {
        final Set<String> propIdx = before.parallelStream().map(prop -> {
            return prop.getName();
        }).collect(Collectors.toSet());

        return Arrays.asList(fields).stream()
                .filter(declaredField -> {
                    return !propIdx.contains(declaredField.getName()) &&
                            Arrays.stream(
                                    declaredField.getModifierList()
                                    .getAnnotations())
                                    .filter(ann -> ann.getQualifiedName().equals("Getter"))
                                    .findFirst()
                                    .isPresent();
                }).map(declaredField -> {
                    List<GenericType> genericTypes = ReflectUtils.buildGenericTypes(declaredField.getType().getCanonicalText());
                    return new GenericVar(declaredField.getName(), genericTypes.get(0), new GenericType[0]);
                }).collect(Collectors.toList());
    }

    private static List<GenericVar> resolveGetters(List<GenericVar> before, PsiMethod[] methods) {
        final Set<String> propIdx = before.parallelStream().map(prop -> {
            return prop.getName();
        }).collect(Collectors.toSet());

        return Arrays.stream(methods).filter(m -> {
            return
                    m.getParameterList().getParameters().length == 0 &&
                            m.getName().startsWith("get") &&
                            m.hasModifierProperty(PsiModifier.PUBLIC);
        }).map(m -> {
            String name = m.getName().replaceFirst("get", "");
            String prefix = name.substring(0, 1);
            String postFix = name.substring(1);
            name = prefix.toLowerCase() + postFix;

            return new GenericVar(name, ReflectUtils.buildGenericTypes(m.getReturnType().getCanonicalText()).get(0),
                    new GenericType[0]);
        }).collect(Collectors.toList());
    }


}
