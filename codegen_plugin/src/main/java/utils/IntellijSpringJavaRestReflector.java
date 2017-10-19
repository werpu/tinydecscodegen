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
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import reflector.utils.ReflectUtils;
import rest.GenericClass;
import rest.GenericType;
import rest.GenericVar;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Reflector class which uses Intellijs source code reflection api (Psi Api)
 * For now only this one is used.
 * We might add bytecode reflection wherever the sources are not reachable anymore,
 * in the future.
 */
public class IntellijSpringJavaRestReflector {


    public static List<GenericClass> reflectDto(List<PsiClass> toReflect, String includingEndpoint) {

        return toReflect.stream().filter(clazz -> clazz.hasModifierProperty(PsiModifier.PUBLIC)).map(clazz -> {
            GenericClass parent = null;
            Collection<GenericVar> props = inheritanceWalker(clazz, includingEndpoint);
            if (includingEndpoint.equals(clazz.getQualifiedName()) && clazz.getSuperClass() != null && !clazz.getSuperClass().getQualifiedName().equals(Object.class.getName())) {
                parent = reflectDto(Arrays.asList(clazz.getSuperClass()), includingEndpoint).get(0);
            }
            GenericType classDescriptor = ReflectUtils.buildGenericTypes(clazz.getQualifiedName()).get(0);

            return new GenericClass(classDescriptor, parent, Collections.emptyList(), props.stream().collect(Collectors.toList()));

        }).collect(Collectors.toList());
    }


    private static Collection<GenericVar> inheritanceWalker(PsiClass clazz, String includingEndoint) {

        Collection<GenericVar> retVal = getAllProperties(clazz);

        PsiClass parent = clazz.getSuperClass();


        while(parent != null && !parent.getQualifiedName().equals(includingEndoint) && !parent.getQualifiedName().startsWith("java.")) {
            final Set<String> propIdx = buildIdx(retVal);
            retVal.addAll(getAllProperties(parent).stream().filter(element -> !propIdx.contains(element.getName())).collect(Collectors.toList()));
            parent = parent.getSuperClass();
        }

        //TODO inheritance somewhere in the binaries not in the sources

        return retVal;
    }

    public static Collection<GenericVar> getAllProperties(PsiClass clazz) {

        PsiField[] fields = clazz.getAllFields();

        PsiMethod[] methods = clazz.getAllMethods();


        List<GenericVar> retVal = Lists.newArrayListWithCapacity(30);
        retVal.addAll(resolveClassLombok(Collections.emptyList(), clazz));
        retVal.addAll(resolveGetters(retVal, methods));
        retVal.addAll(resolveLombok(retVal, fields));
        retVal.addAll(resolvePublicProps(retVal, fields));
        return retVal.stream()
                .sorted(Comparator.comparing(GenericVar::getName))
                .collect(Collectors.toList());

    }

    private static List<GenericVar> resolvePublicProps(List<GenericVar> before, PsiField[] fields) {
        final Set<String> propIdx = buildIdx(before);

        return Arrays.asList(fields).stream()
                .filter(declaredField -> {
                    return !propIdx.contains(declaredField.getName()) && declaredField.hasModifierProperty(PsiModifier.PUBLIC)
                            && !declaredField.hasModifierProperty(PsiModifier.STATIC);
                }).map(IntellijSpringJavaRestReflector::remapField).collect(Collectors.toList());
    }

    @NotNull
    private static GenericVar remapField(PsiField declaredField) {

            List<GenericType> genericTypes = ReflectUtils.buildGenericTypes(declaredField.getType().getCanonicalText());
            return new GenericVar(declaredField.getName(), genericTypes.get(0), new GenericType[0]);

    }

    private static List<GenericVar> resolveClassLombok(List<GenericVar> before, PsiClass clazz) {
        final Set<String> propIdx = buildIdx(before);
        if(isLombokedClass(clazz)) {
            return Arrays.stream(clazz.getAllFields())
                    .filter(declaredField -> !propIdx.contains(declaredField.getName()))
                    .map(IntellijSpringJavaRestReflector::remapField)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


    private static List<GenericVar> resolveLombok(List<GenericVar> before, PsiField[] fields) {
        final Set<String> propIdx = buildIdx(before);

        return Arrays.asList(fields).stream()
                .filter(declaredField -> !propIdx.contains(declaredField.getName()) &&
                        isLomboked(declaredField)).map(declaredField -> {
                    List<GenericType> genericTypes = ReflectUtils.buildGenericTypes(declaredField.getType().getCanonicalText());
                    return new GenericVar(declaredField.getName(), genericTypes.get(0), new GenericType[0]);
                }).collect(Collectors.toList());
    }

    private static boolean isLombokedClass(PsiClass clazz) {
        return Arrays.stream(clazz.getModifierList()
                .getAnnotations())
                .filter(ann -> isLombokedAnn(ann))
                .findFirst()
                .isPresent();
    }

    private static boolean isLomboked(PsiField declaredField) {
        return Arrays.stream(
                declaredField.getModifierList()
                .getAnnotations())
                .filter(ann -> isLombokedAnn(ann))
                .findFirst()
                .isPresent();
    }

    private static boolean isLombokedAnn(PsiAnnotation ann) {
        return ann.getQualifiedName().equals("Getter") || ann.getQualifiedName().equals("Data");
    }

    private static List<GenericVar> resolveGetters(List<GenericVar> before, PsiMethod[] methods) {
        final Set<String> propIdx = buildIdx(before);

        return Arrays.stream(methods).filter(m -> m.getParameterList().getParameters().length == 0 &&
                        m.getName().startsWith("get") &&
                        m.hasModifierProperty(PsiModifier.PUBLIC)).map(m -> {
            String name = m.getName().replaceFirst("get", "");
            String prefix = name.substring(0, 1);
            String postFix = name.substring(1);
            name = prefix.toLowerCase() + postFix;

            return new GenericVar(name, ReflectUtils.buildGenericTypes(m.getReturnType().getCanonicalText()).get(0),
                    new GenericType[0]);
        }).filter(genericVar -> !propIdx.contains(genericVar.getName())).collect(Collectors.toList());
    }

    private static Set<String> buildIdx(Collection<GenericVar> before) {
        return before.stream().map(prop -> {
                return prop.getName();
            }).collect(Collectors.toSet());
    }


}
