package supportive.reflectRefact;

import com.google.common.base.Strings;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.impl.source.tree.CompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static supportive.reflectRefact.IntellijRefactor.NG_MODULE;
import static supportive.utils.StringUtils.elVis;

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
    public static final String JS_REFERENCE_EXPRESSION = "JSReferenceExpression";
    public static final String JS_ES_6_DECORATOR = "ES6Decorator";
    public static final String NG_COMPONENT = "@Component";
    public static final String TN_CONTROLLER = "@Controller";
    public static final String TYPE_SCRIPT_CLASS = "TypeScriptClass";
    public static final String PSI_METHOD = "PsiMethod:";
    public static final String JS_ES_6_IMPORT_DECLARATION = "ES6ImportDeclaration";


    public static boolean isNgModule(PsiElement element) {
        return isAnnotatedElement(element, NG_MODULE);
    }

    public static boolean isAnnotatedElement(PsiElement element, String annotatedElementType) {
        return element != null && !Strings.isNullOrEmpty(element.getText()) &&
                element.getText().startsWith(annotatedElementType) &&
                element.getClass().getName().equals(JS_DECORATOR_IMPL);
    }

    public static boolean isImport(PsiElement element) {
        return element != null && element.toString().equals(JS_ES_6_IMPORT_DECLARATION);
    }


    public static boolean isMethod(PsiElement element) {
        return element != null && element.toString().startsWith(PSI_METHOD);
    }

    public static boolean isClass(PsiElement element) {
        return element != null && element.toString().startsWith(TYPE_SCRIPT_CLASS);
    }

    /**
     * detect a template in the psi treee
     * <p>
     * a template is an element which has the js property template
     * and is embedded in a component or directive annotation
     *
     * @param element the element to check
     * @return true if the element is a template element
     */
    public static boolean isTemplate(PsiElement element) {
        return (element != null
                && getName(element).equals(JS_PROP_TYPE)
                && getText(element).equals(JS_PROP_TEMPLATE)
                && (isIn(element, NG_TYPE_COMPONENT)
                || isIn(element, NG_TYPE_DIRECTIVE)
                || isIn(element, NG_TYPE_CONTROLLER)));
    }

    public static boolean isTemplateRef(PsiElement element) {
        return element != null && element.toString().equals(JS_REFERENCE_EXPRESSION);
    }

    public static boolean isTemplateString(PsiElement element) {
        return element != null && getName(element).equals(JS_STRING_TYPE);
    }

    public static boolean isComponent(PsiElement element) {
        return element != null &&
                element.toString().startsWith(JS_ES_6_DECORATOR) &&
                element.getText().startsWith(NG_COMPONENT);
    }

    public static boolean isController(PsiElement element) {
        return element != null &&
                element.toString().startsWith(JS_ES_6_DECORATOR) &&
                element.getText().startsWith(TN_CONTROLLER);
    }

    public static boolean isTypeScriptClass(PsiElement element) {
        return element != null && element.toString().startsWith(TYPE_SCRIPT_CLASS);
    }

    @NotNull
    private static String getText(PsiElement element) {
        return (String) elVis(element, "node", "firstChildNode", "text").get();
    }


    @Nullable
    private static String getName(PsiElement element) {
        return (String) elVis(element, "node", "elementType", "class", "name").orElseGet(null);
    }

    /**
     * checks whether a certain string one of the psi elements
     *
     * @param element
     * @param type
     * @return
     */
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

    /**
     * Psi file walker
     * does the same as the other walkPsiTree method
     *
     * @param elem
     * @param psiElementVisitor
     * @param firstOnly
     * @return
     */
    public static List<PsiElement> walkPsiTree(PsiFile elem, Function<PsiElement, Boolean> psiElementVisitor, boolean firstOnly) {
        final List<PsiElement> retVal = new LinkedList<>();
        PsiRecursiveElementWalkingVisitor myElementVisitor = createPsiVisitor(psiElementVisitor, firstOnly, retVal);
        myElementVisitor.visitFile(elem);

        return retVal;
    }

    /**
     * standardized walk the tree function which takes
     * a filter to prefilter the psi elements
     * which then get processed from the outside
     *
     * @param elem
     * @param psiElementVisitor
     * @param firstOnly
     * @return
     */
    public static List<PsiElement> walkPsiTree(PsiElement elem, Function<PsiElement, Boolean> psiElementVisitor, boolean firstOnly) {
        final List<PsiElement> retVal = new LinkedList<>();
        PsiRecursiveElementWalkingVisitor myElementVisitor = createPsiVisitor(psiElementVisitor, firstOnly, retVal);
        myElementVisitor.visitElement(elem);

        return retVal;
    }

    /**
     * Standardized psi tree visitor
     * used literally by all psi streams to walk the tree
     *
     * @param psiElementVisitor
     * @param firstOnly
     * @param retVal
     * @return
     */
    @NotNull
    public static PsiRecursiveElementWalkingVisitor createPsiVisitor(Function<PsiElement, Boolean> psiElementVisitor, boolean firstOnly, List<PsiElement> retVal) {
        return new PsiRecursiveElementWalkingVisitor() {

            public void visitElement(PsiElement element) {


                if (psiElementVisitor.apply(element)) {
                    retVal.add(element);
                    if (firstOnly) {
                        stopWalking();
                    }
                    return;
                }
                super.visitElement(element);
            }
        };
    }
}
