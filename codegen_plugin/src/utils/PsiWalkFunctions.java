package utils;

import actions.shared.JavaFileContext;
import com.google.common.base.Strings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import org.jetbrains.annotations.NotNull;

import static utils.IntellijRefactor.NG_MODULE;

public class PsiWalkFunctions {

    public static final String JS_PROP_TYPE = "com.intellij.lang.javascript.types.JSPropertyElementType";
    public static final String JS_PROP_TEMPLATE = "template";
    public static final String NG_TYPE_COMPONENT = "Component";
    public static final String NG_TYPE_DIRECTIVE = "Directive";
    public static final String JS_STRING_TYPE = "com.intellij.lang.javascript.types.JSStringTemplateExpressionElementType";
    public static final String NG_TYPE_CONTROLLER = "Controller";

    public static boolean isNgModule(PsiElement element) {
        return isAnnotatedElement(element, NG_MODULE);
    }

    public static boolean isAnnotatedElement(PsiElement element, String annotatedElementType) {
        return element != null && !Strings.isNullOrEmpty(element.getText()) && element.getText().startsWith(annotatedElementType) && element.getClass().getName().equals("com.intellij.lang.typescript.psi.impl.ES6DecoratorImpl");
    }

    /**
     * detect a temmplate in the psi treee
     *
     * a template is an element which has the js property template
     * and is embedded in a component or directive annotation
     *
     * @param element the element to check
     * @return true if the element is a template element
     */
    public static boolean isTemplate(PsiElement element) {

        if (element != null && getName(element).equals(JS_PROP_TYPE)
                && getText(element).equals(JS_PROP_TEMPLATE)
                && (isIn(element, NG_TYPE_COMPONENT) ||
                isIn(element, NG_TYPE_DIRECTIVE) ||
                isIn(element, NG_TYPE_CONTROLLER))) {
            return true;
        }
        return false;
    }

    public static boolean isTemplateString(PsiElement element) {
        System.out.println(getName(element));
        if(element != null && getName(element).equals(JS_STRING_TYPE)) {
            return true;
        }

        return false;
    }

    @NotNull
    private static String getText(PsiElement element) {
        return ((CompositeElement) element.getNode()).getFirstChildNode().getText();
    }

    private static String getName(PsiElement element) {
        return element.getNode().getElementType().getClass().getName();
    }

    private static  boolean isIn(PsiElement element, String type) {

        for(int cnt = 0; cnt < 3; cnt++) {
            if(element == null) {
                return false;
            }
            element = element.getParent();
        }
        if(element.getNode() == null) {
            return false;
        }
        return (((CompositeElement) element.getNode())).getFirstChildNode().getText().equals(type);
    }
}
