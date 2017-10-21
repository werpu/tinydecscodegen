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

import com.google.common.base.Strings;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;
import reflector.utils.ReflectUtils;
import rest.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Intellij psi based spring rest service reflector class
 */
public class IntellijSpringRestReflector {

    public static List<RestService> reflectRestService(List<PsiClass> toReflect, boolean flattenResult, int returnValueNestingDepth) {
        return toReflect.stream()
                .filter(IntellijSpringRestReflector::isRestService)
                .map(cls -> {
                    String serviceUrl = fetchServiceUrl(cls);
                    List<RestMethod> restMethods = fetchRestMethods(cls, flattenResult, returnValueNestingDepth);
                    return new RestService(fetchServiceName(cls), serviceUrl, cls.getQualifiedName(), restMethods);
                }).collect(Collectors.toList());
    }


    private static boolean isRestService(PsiClass cls) {
        return isPresent(cls, RestController.class);
    }


    private static String fetchServiceUrl(PsiClass cls) {

        if (isPresent(cls, RequestMapping.class)) {
            PsiAnnotation ann = getAnn(cls, RequestMapping.class);
            String val = (ann.findAttributeValue("value") != null) ? ann.findAttributeValue("value").getText() : "";

            return getAttr(ann, "path", val);
        }
        return "";
    }

    private static String fetchServiceName(PsiClass cls) {

        if (isPresent(cls, RequestMapping.class)) {
            PsiAnnotation ann = getAnn(cls, RequestMapping.class);
            return getAttr(ann, "name", cls.getName().replace("Controller", "Service"));
        }
        return "";
    }

    private static List<RestMethod> fetchRestMethods(PsiClass cls, boolean flattenResult, int returnValueNestingDepth) {
        PsiMethod[] allMethods = cls.getAllMethods();
        return Arrays.stream(allMethods)
                .filter(IntellijSpringRestReflector::isRestMethod)
                .map(m -> mapMethod(m, flattenResult, returnValueNestingDepth))
                .sorted(Comparator.comparing(RestMethod::getName))
                .collect(Collectors.toList());
    }


    private static RestMethod mapMethod(PsiMethod m, boolean flattenResult, int returnValueNestingDepth) {
        String name = m.getName();
        PsiAnnotation mapping = getAnn(m, RequestMapping.class);
        String path = getAttr(mapping, "value");

        //TODO multiple methods possible
        String method = getAttr(mapping, "method");
        //TODO multiple consumes possible
        if(method.contains(".")) {
            method = method.substring(method.lastIndexOf(".")+1);
        }

        String comment = m.getDocComment() != null ? m.getDocComment().getText(): "";
        String consumes = getAttr(mapping, "consumes");
        List<RestVar> params = getRestVars(m);

        String sReturnType = m.getReturnType().getCanonicalText();
        for(int cnt = 0; cnt < returnValueNestingDepth; cnt++) {
            int from = sReturnType.indexOf("<");
            int to = sReturnType.lastIndexOf(">");
            if(from == -1 || to == -1) {
                break;
            }
            sReturnType = sReturnType.substring(from+1, to);
        }
        List<GenericType> childTypes = ReflectUtils.buildGenericTypes(sReturnType);
        GenericType genericTypes = new GenericType("", childTypes);
        RestVar returnType = new RestVar(RestVarType.RequestRetval, null, null,
                ReflectUtils.isArrayType(sReturnType),
                null, genericTypes);

        if(Strings.isNullOrEmpty(method)) {
            method = RestType.GET.name();
        }
        return new RestMethod(path, name, RestType.valueOf(method.toUpperCase()), Optional.ofNullable(returnType), params, comment);
    }

    private static String getAttr(PsiAnnotation mapping, String attrVal) {
        PsiAnnotationMemberValue attributeValue = mapping.findAttributeValue(attrVal);
        String retVal = (attributeValue != null) ? attributeValue.getText() : "";
        retVal = fixText(retVal);
        return retVal;
    }

    private static String getAttr(PsiAnnotation mapping, String attrVal, String theDefault) {
        PsiAnnotationMemberValue attributeValue = mapping.findAttributeValue(attrVal);
        String retVal = (attributeValue != null) ? attributeValue.getText() : "";
        retVal = fixText(retVal);
        retVal = Strings.isNullOrEmpty(retVal) ? theDefault : retVal;
        retVal = fixText(retVal);
        return retVal;
    }

    private static String fixText(String retVal) {
        if(retVal.indexOf("/{") != -1) {
            retVal = retVal.substring(0, retVal.indexOf("/{"));
        }
        retVal = retVal.replaceAll("[\\\"\\{\\}]+", "");
        return retVal;
    }


    public static List<RestVar> getRestVars(PsiMethod m) {
        return Arrays.stream(m.getParameterList().getParameters())
                .filter(p -> isPresent(p, PathVariable.class, RequestBody.class, RequestParam.class))
                .map(p -> getRestVar(m, p)).collect(Collectors.toList());

    }

    @NotNull
    private static RestVar getRestVar(PsiMethod m, PsiParameter p) {
        PsiAnnotation ann = getAnn(p, PathVariable.class, RequestBody.class, RequestParam.class);
        RestVarType restVarType = null;
        String restName = "";
        String name = p.getName();
        if (ann.getQualifiedName().endsWith(RestVarType.PathVariable.name())) {
            restVarType = RestVarType.PathVariable;
        } else if (ann.getQualifiedName().endsWith(RestVarType.RequestParam.name())) {
            restVarType = RestVarType.RequestParam;
            restName = getAttr(ann, "value");
        } else {
            restVarType = RestVarType.RequestBody;
        }
        boolean array = ReflectUtils.isArrayType(p.getTypeElement().getType().getCanonicalText());
        return new RestVar(restVarType, restName, name, new GenericType(p.getType().getCanonicalText(), Collections.emptyList()), array);
    }

    private static boolean isRestMethod(PsiMethod m) {
        return Arrays.stream(m.getModifierList().getAnnotations())
                .anyMatch(ann -> isPresent(ann, RequestMapping.class));
    }


    private static boolean isPresent(PsiModifierListOwner ownwer, Class... annotation) {
        return Arrays.stream(ownwer.getModifierList().getAnnotations())
                .anyMatch(ann -> isPresent(ann, annotation));
    }

    private static PsiAnnotation getAnn(PsiModifierListOwner owner, Class... annotation) {
        return Arrays.stream(owner.getModifierList().getAnnotations())
                .filter(ann -> isPresent(ann, annotation))
                .findFirst().get();
    }

    private static boolean isPresent(PsiAnnotation ann, Class... annotation) {
        return Arrays.asList(annotation).stream().map(ann2 -> ann2.getName())
                .anyMatch(name -> name.contains(ann.getQualifiedName()));
    }


}
