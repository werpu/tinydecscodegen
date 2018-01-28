package utils;

import com.google.common.base.Strings;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import org.jetbrains.annotations.NotNull;
import reflector.utils.ReflectUtils;
import rest.GenericType;
import rest.RestVar;
import rest.RestVarType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class PsiAnnotationUtils {

    public static String getAttr(PsiAnnotation mapping, String attrVal) {
        PsiAnnotationMemberValue attributeValue = mapping.findAttributeValue(attrVal);
        String retVal = (attributeValue != null) ? attributeValue.getText() : "";
        retVal = fixText(retVal);
        return retVal;
    }

    public static String getAttr(PsiAnnotation mapping, String attrVal, String theDefault) {
        PsiAnnotationMemberValue attributeValue = mapping.findAttributeValue(attrVal);
        String retVal = (attributeValue != null) ? attributeValue.getText() : "";
        retVal = fixText(retVal);
        retVal = Strings.isNullOrEmpty(retVal) ? theDefault : retVal;
        retVal = fixText(retVal);
        return retVal;
    }

    private static String fixText(String retVal) {
        if (retVal.indexOf("/{") != -1) {
            retVal = retVal.substring(0, retVal.indexOf("/{"));
        }
        retVal = retVal.replaceAll("[\\\"\\{\\}]+", "");
        return retVal;
    }

    public static boolean isPresent(PsiModifierListOwner ownwer, Class... annotation) {
        return Arrays.stream(ownwer.getModifierList().getAnnotations())
                .anyMatch(ann -> isPresent(ann, annotation));
    }

    public static PsiAnnotation getAnn(PsiModifierListOwner owner, Class... annotation) {
        return Arrays.stream(owner.getModifierList().getAnnotations())
                .filter(ann -> isPresent(ann, annotation))
                .findFirst().orElse(null);
    }



    public static boolean isPresent(PsiAnnotation ann, Class... annotation) {
        return Arrays.asList(annotation).stream().map(ann2 -> ann2.getName())
                .anyMatch(name -> name.contains(ann.getQualifiedName()));
    }

    @NotNull
    static RestVar getRestReturnType(PsiMethod m, int returnValueNestingDepth) {
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

        if(sReturnType.equals("Response") || sReturnType.equals("ResponseEntity")) {
            //TODO source parsing of the java source
            sReturnType = "any";
        }
        return new RestVar(RestVarType.RequestRetval, null, null,
                ReflectUtils.isArrayType(sReturnType),
                null, genericTypes);
    }
}
