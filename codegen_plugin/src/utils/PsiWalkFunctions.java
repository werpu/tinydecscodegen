package utils;

import com.google.common.base.Strings;
import com.intellij.psi.PsiElement;

import static utils.IntellijRefactor.NG_MODULE;

public class PsiWalkFunctions {

    public static boolean isNgModule(PsiElement element) {
        return isAnnotatedElement(element, NG_MODULE);
    }

    public static boolean isAnnotatedElement(PsiElement element, String annotatedElementType) {
        return element != null && !Strings.isNullOrEmpty(element.getText()) && element.getText().startsWith(annotatedElementType) && element.getClass().getName().equals("com.intellij.lang.typescript.psi.impl.ES6DecoratorImpl");
    }

    public static boolean isTemplate(PsiElement element) {

        if (element != null && element.getText().equals("template") && element.getNode().getElementType().equals("JS:IDENTIFIER")
                && element.getParent().getParent().getParent().getParent().getText().startsWith("Component")
                ) {
            return true;
        }
        return false;

    }
}
