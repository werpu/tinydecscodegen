package utils;

import com.google.common.base.Strings;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import org.jetbrains.annotations.NotNull;

import static utils.IntellijRefactor.NG_MODULE;

public class PsiWalkFunctions {

    public static final String JS_PROP_TYPE = "com.intellij.lang.javascript.types.JSPropertyElementType";

    //var for include
    public static final String JS_DECORATOR_IMPL = "com.intellij.lang.typescript.psi.impl.ES6DecoratorImpl";
    public static final String JS_STRING_TYPE = "com.intellij.lang.javascript.types.JSStringTemplateExpressionElementType";

    //under investigation
    public static final String JS_IMPORT_DCL = "com.intellij.lang.typescript.psi.impl.ES6ImportDeclaration";
    public static final String JS_IMPORT_SPEC = "com.intellij.lang.typescript.psi.impl.ES6ImportSpecifier";
    public static final String JS_FROM_CLAUSE = "com.intellij.lang.typescript.psi.impl.ES6FromClause";
    public static final String JS_REF_TYPE = "com.intellij.lang.typescript.psi.impl.ES6ReferenceExpression";


    public static final String JS_PROP_TEMPLATE = "template";
    public static final String NG_TYPE_COMPONENT = "Component";
    public static final String NG_TYPE_DIRECTIVE = "Directive";
    public static final String NG_TYPE_CONTROLLER = "Controller";


    public static boolean isNgModule(PsiElement element) {
        return isAnnotatedElement(element, NG_MODULE);
    }

    public static boolean isAnnotatedElement(PsiElement element, String annotatedElementType) {
        return element != null && !Strings.isNullOrEmpty(element.getText()) &&
                element.getText().startsWith(annotatedElementType) &&
                element.getClass().getName().equals(JS_DECORATOR_IMPL);
    }

    public static boolean isImport(PsiElement element) {
        return element != null && element.toString().equals("ES6ImportDeclaration");
    }

    public static boolean isMethod(PsiElement element) {
        return element != null && element.toString().startsWith("PsiMethod:");
    }

    public static boolean isClass(PsiElement element) {
        return element != null && element.toString().startsWith("PsiClass:");
    }

    /**
     * detect a temmplate in the psi treee
     * <p>
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

    public static boolean isTemplateRef(PsiElement element) {
        return element != null && element.toString().equals("JSReferenceExpression");
    }

    public static boolean isTemplateString(PsiElement element) {
        return element != null && getName(element).equals(JS_STRING_TYPE);
    }

    @NotNull
    private static String getText(PsiElement element) {
        return ((CompositeElement) element.getNode()).getFirstChildNode().getText();
    }

    private static String getName(PsiElement element) {
        return element.getNode().getElementType().getClass().getName();
    }

    private static boolean isIn(PsiElement element, String type) {

        for (int cnt = 0; cnt < 3; cnt++) {
            if (element == null) {
                return false;
            }
            element = element.getParent();
        }
        if (element.getNode() == null) {
            return false;
        }
        return (((CompositeElement) element.getNode())).getFirstChildNode().getText().equals(type);
    }
}
