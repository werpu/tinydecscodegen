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
package net.werpu.tools.supportive.reflectRefact;

import com.google.common.base.Strings;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
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
    public static boolean isRestService(List<PsiClass> toReflect) {
        return toReflect.stream().filter(IntellijSpringRestReflector::isRestService).findFirst().isPresent();
    }

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
        return PsiAnnotationUtils.isPresent(cls, RestController.class);
    }

    private static String fetchServiceUrl(PsiClass cls) {

        if (PsiAnnotationUtils.isPresent(cls, RequestMapping.class)) {
            PsiAnnotation ann = PsiAnnotationUtils.getAnn(cls, RequestMapping.class);
            String val = (ann.findAttributeValue("value") != null) ? ann.findAttributeValue("value").getText() : "";

            return PsiAnnotationUtils.getAttr(ann, "path", val);
        }
        return "";
    }

    private static String fetchServiceName(PsiClass cls) {

        if (PsiAnnotationUtils.isPresent(cls, RequestMapping.class)) {
            PsiAnnotation ann = PsiAnnotationUtils.getAnn(cls, RequestMapping.class);
            return PsiAnnotationUtils.getAttr(ann, "name", cls.getName().replace("Controller", "Service"));
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

    public static List<RestVar> getRestVars(PsiMethod m) {
        return Arrays.stream(m.getParameterList().getParameters())
                .filter(p -> PsiAnnotationUtils.isPresent(p, PathVariable.class, RequestBody.class, RequestParam.class))
                .map(p -> getRestVar(m, p)).collect(Collectors.toList());

    }

    @NotNull
    private static RestVar getRestVar(PsiMethod m, PsiParameter p) {
        PsiAnnotation ann = PsiAnnotationUtils.getAnn(p, PathVariable.class, RequestBody.class, RequestParam.class);
        RestVarType restVarType = null;
        String restName = "";
        String name = p.getName();
        if (ann.getQualifiedName().endsWith(RestVarType.PathVariable.name())) {
            restVarType = RestVarType.PathVariable;
        } else if (ann.getQualifiedName().endsWith(RestVarType.RequestParam.name())) {
            restVarType = RestVarType.RequestParam;
            restName = PsiAnnotationUtils.getAttr(ann, "value");
        } else {
            restVarType = RestVarType.RequestBody;
        }
        boolean array = ReflectUtils.isArrayType(p.getTypeElement().getType().getCanonicalText());
        return new RestVar(restVarType, restName, name, new GenericType(p.getType().getCanonicalText(), Collections.emptyList()), array);
    }

    private static boolean isRestMethod(PsiMethod m) {
        return Arrays.stream(m.getModifierList().getAnnotations())
                .anyMatch(ann -> PsiAnnotationUtils.isPresent(ann, RequestMapping.class));
    }

    static RestMethod mapMethod(PsiMethod m, boolean flattenResult, int returnValueNestingDepth) {
        String name = m.getName();
        PsiAnnotation mapping = PsiAnnotationUtils.getAnn(m, RequestMapping.class);
        String path = PsiAnnotationUtils.getAttr(mapping, "value");

        //TODO multiple methods possible
        String method = PsiAnnotationUtils.getAttr(mapping, "method");
        //TODO multiple consumes possible
        if (method.contains(".")) {
            method = method.substring(method.lastIndexOf(".") + 1);
        }

        String comment = m.getDocComment() != null ? m.getDocComment().getText() : "";
        String consumes = PsiAnnotationUtils.getAttr(mapping, "consumes");
        List<RestVar> params = getRestVars(m);

        RestVar returnType = PsiAnnotationUtils.getRestReturnType(m, returnValueNestingDepth);

        if (Strings.isNullOrEmpty(method)) {
            method = RestType.GET.name();
        }
        return new RestMethod(path, name, RestType.valueOf(method.toUpperCase()), Optional.ofNullable(returnType), params, comment);
    }

}
