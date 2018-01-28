package utils;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import org.jetbrains.annotations.NotNull;
import reflector.utils.ReflectUtils;
import rest.*;

import javax.ws.rs.*;
import java.util.*;
import java.util.stream.Collectors;

import static utils.PsiAnnotationUtils.*;

/**
 * placeholder for the jaxrs reflector
 */
public class IntellijJaxRsReflector {

    public boolean isRestService(List<PsiClass> toReflect) {
        return toReflect.stream().filter(IntellijJaxRsReflector::isRestService).findFirst().isPresent();
    }

    public static List<RestService> reflectRestService(List<PsiClass> toReflect, boolean flattenResult, int returnValueNestingDepth) {
        return toReflect.stream().filter(IntellijJaxRsReflector::isRestService)
                .map(cls -> {
                    String serviceUrl = fetchServiceUrl(cls);
                    List<RestMethod> restMethods = fetchRestMethods(cls, flattenResult, returnValueNestingDepth);
                    return new RestService(Objects.requireNonNull(cls.getName()).replace("Controller", "Service"), serviceUrl, cls.getQualifiedName(), restMethods);
                }).collect(Collectors.toList());
    }


    private static String fetchServiceUrl(PsiClass cls) {

        if (isPresent(cls, Path.class)) {
            PsiAnnotation ann = getAnn(cls, Path.class);
            String val = (ann.findAttributeValue("value") != null) ? Objects.requireNonNull(ann.findAttributeValue("value")).getText() : "";

            return getAttr(ann, "path", val);
        }
        return "";
    }


    private static List<RestMethod> fetchRestMethods(PsiClass cls, boolean flattenResult, int returnValueNestingDepth) {
        PsiMethod[] allMethods = cls.getAllMethods();
        return Arrays.stream(allMethods)
                .filter(IntellijJaxRsReflector::isRestMethod)
                .map(m -> mapMethod(m, flattenResult, returnValueNestingDepth))
                .sorted(Comparator.comparing(RestMethod::getName))
                .collect(Collectors.toList());
    }

    private static boolean isRestService(PsiClass cls) {
        return isPresent(cls, Path.class) || hasRestMethod(cls);
    }

    private static boolean hasRestMethod(PsiClass cls) {
        PsiMethod[] allMethods = cls.getAllMethods();
        return Arrays.stream(allMethods).anyMatch(IntellijJaxRsReflector::isRestMethod);
    }

    private static boolean isRestMethod(PsiMethod m) {
        return Arrays.stream(m.getModifierList().getAnnotations())
                .anyMatch(ann -> isPresent(ann, Path.class, GET.class, PUT.class, DELETE.class, POST.class, Consumes.class, Produces.class));
    }


    private static List<RestVar> getRestVars(PsiMethod m) {
        return Arrays.stream(m.getParameterList().getParameters())
                // .filter(p -> PsiAnnotationUtils.isPresent(p, PathParam.class, QueryParam.class))
                .map(IntellijJaxRsReflector::getRestVar).collect(Collectors.toList());

    }


    @NotNull
    private static RestVar getRestVar(PsiParameter p) {
        PsiAnnotation ann = PsiAnnotationUtils.getAnn(p, PathParam.class, QueryParam.class);
        RestVarType restVarType;
        String restName = "";
        String name = p.getName();
        if (ann != null && Objects.requireNonNull(ann.getQualifiedName()).endsWith(RestVarType.PathParam.name())) {
            restVarType = RestVarType.PathVariable;
        } else if (ann != null && ann.getQualifiedName().endsWith(RestVarType.QueryParam.name())) {
            restVarType = RestVarType.RequestParam;
            restName = PsiAnnotationUtils.getAttr(ann, "value");
        } else {
            restVarType = RestVarType.RequestBody;
        }
        boolean array = ReflectUtils.isArrayType(Objects.requireNonNull(p.getTypeElement()).getType().getCanonicalText());
        return new RestVar(restVarType, restName, name, new GenericType(p.getType().getCanonicalText(), Collections.emptyList()), array);
    }

    private static RestMethod mapMethod(PsiMethod m, boolean flattenResult, int returnValueNestingDepth) {
        String name = m.getName();
        PsiAnnotation mapping = PsiAnnotationUtils.getAnn(m, Path.class);
        String path = PsiAnnotationUtils.getAttr(mapping, "value");

        int firstParam = path.indexOf("/{");
        if (firstParam == -1) {
            firstParam = path.length();
        }
        String comment = m.getDocComment() != null ? m.getDocComment().getText() : "";

        path = path.substring(0, firstParam);

        List<RestVar> params = getRestVars(m);

        RestVar returnType = PsiAnnotationUtils.getRestReturnType(m, returnValueNestingDepth);

        PsiAnnotation ann = PsiAnnotationUtils.getAnn(m, GET.class, POST.class, PUT.class, DELETE.class);

        String method = ann.getQualifiedName();

        return new RestMethod(path, name, RestType.valueOf(Objects.requireNonNull(method).toUpperCase()), Optional.of(returnType), params, comment);

    }


}
