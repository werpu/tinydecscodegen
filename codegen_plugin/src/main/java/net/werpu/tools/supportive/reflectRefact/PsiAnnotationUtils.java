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
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import org.jetbrains.annotations.NotNull;
import reflector.utils.ReflectUtils;
import rest.GenericType;
import rest.RestVar;
import rest.RestVarType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class PsiAnnotationUtils {

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

    /**
     * returns a filter function which allows to filter the element at the precise position
     * @return
     */
    public static  Predicate<PsiElementContext> getPositionFilter(int cursorPos) {
        Predicate<PsiElementContext> positionFilter = el -> {
            int offSet = el.getTextRangeOffset();
            int offSetEnd = offSet + el.getText().length();
            return cursorPos >= offSet && cursorPos <= offSetEnd;
        };
        return positionFilter;
    }
    //in case of a simple html file we simply can proceed from there

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

        if(sReturnType.equals("Response") || sReturnType.equals("ResponseEntity")) {
            //TODO source parsing of the java source
            sReturnType = "any";
        }

        List<GenericType> childTypes = ReflectUtils.buildGenericTypes(sReturnType);
        GenericType genericTypes = new GenericType("", childTypes);


        return new RestVar(RestVarType.RequestRetval, null, null,
                ReflectUtils.isArrayType(sReturnType),
                null, genericTypes);
    }
}
