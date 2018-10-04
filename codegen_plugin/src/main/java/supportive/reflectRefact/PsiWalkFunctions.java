package supportive.reflectRefact;

import com.google.common.base.Strings;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.impl.source.tree.CompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.PsiElementContext;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static supportive.reflectRefact.IntellijRefactor.NG_MODULE;
import static supportive.utils.StringUtils.*;

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
    public static final String JS_EXPRESSION_STATEMENT = "JSExpressionStatement";
    public static final String JS_BLOCK_ELEMENT = "JSBlockElement";
    public static final String JS_BLOCK_STATEMENT = "JSBlockStatement";
    public static final String JS_OBJECT_LITERAL_EXPRESSION = "JSObjectLiteralExpression";
    public static final String JS_ES_6_DECORATOR = "ES6Decorator";
    public static final String JS_ES_6_FROM_CLAUSE = "ES6FromClause";
    public static final String NG_COMPONENT = "@Component";
    public static final String NG_INJECT = "@Inject";
    public static final String TN_CONFIG = "@Config";
    public static final String TN_CONTROLLER = "@Controller";
    public static final String TYPE_SCRIPT_CLASS = "TypeScriptClass";
    public static final String TYPE_SCRIPT_VARIABLE = "TypeScriptVariable";
    public static final String TYPE_SCRIPT_PARAM = "TypeScriptParameter";
    public static final String TYPE_SCRIPT_FUNC = "TypeScriptFunction";
    public static final String PSI_METHOD = "PsiMethod:";
    public static final String JS_ES_6_IMPORT_DECLARATION = "ES6ImportDeclaration";
    public static final String JS_ES_6_IMPORT_SPECIFIER = "ES6ImportSpecifier";

    public static final String JS_CALL_EXPRESSION = "JSCallExpression";
    public static final String JS_UIROUTER_MODULE_FOR_ROOT = "UIRouterModule.forRoot";
    public static final String JS_ROUTER_MODULE_FOR_ROOT = "$routeProvider.when";
    public static final String JS_STATE_MODULE_FOR_ROOT = "$stateProvider.route";
    public static final String TN_UIROUTER_MODULE_FOR_ROOT = "TN_RootRouter";
    public static final String TN_ROUTES_UIROUTER_MODULE_FOR_ROOT = "TN_RoutesRootRouter";
    public static final String NPM_ROOT = "package.json";
    public static final String PSI_ELEMENT_JS_RBRACKET = "PsiElement(JS:RBRACKET)";
    public static final String PSI_ELEMENT_JS_IDENTIFIER = "PsiElement(JS:IDENTIFIER)";
    public static final String PSI_ELEMENT_JS_STRING_LITERAL = "PsiElement(JS:STRING_LITERAL)";
    public static final String JS_PROPERTY = "JSProperty";
    public static final String JS_ARRAY_LITERAL_EXPRESSION = "JSArrayLiteralExpression";
    public static final String JS_ARGUMENTS_LIST = "JSArgumentList";
    public static final String JS_VAR_STATEMENT = "JSVarStatement";
    public static final String P_PARENTS = ":PARENTS";
    public static final String P_LAST = ":LAST";
    public static final String P_FIRST = ":first";

    public static final String PSI_CLASS = "PsiClass:";


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

    public static boolean isIdentifier(PsiElement element) {
        return element != null && element.toString().startsWith(PSI_ELEMENT_JS_IDENTIFIER);
    }

    public static boolean isStringLiteral(PsiElement element) {
        return element != null && element.toString().startsWith(PSI_ELEMENT_JS_STRING_LITERAL);
    }


    public static boolean isMethod(PsiElement element) {
        return element != null && element.toString().startsWith(PSI_METHOD);
    }

    public static boolean isClass(PsiElement element) {
        return element != null && element.toString().startsWith(TYPE_SCRIPT_CLASS);
    }

    public static boolean isTypeScriptFunc(PsiElement element) {
        return element != null && element.toString().startsWith(TYPE_SCRIPT_FUNC);
    }

    public static boolean isJSExpressionStatement(PsiElement element) {
        return element != null && element.toString().startsWith(JS_EXPRESSION_STATEMENT);
    }

    public static boolean isJSArgumentsList(PsiElement element) {
        return element != null && element.toString().startsWith(JS_ARGUMENTS_LIST);
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


    public static boolean isTnConfig(PsiElement element) {
        return element != null &&
                element.toString().startsWith(JS_ES_6_DECORATOR) &&
                element.getText().startsWith(TN_CONFIG);
    }

    public static boolean isController(PsiElement element) {
        return element != null &&
                element.toString().startsWith(JS_ES_6_DECORATOR) &&
                (
                        element.getText().startsWith(TN_CONTROLLER) ||
                                element.getText().startsWith(NG_COMPONENT)
                );
    }

    public static boolean isPsiClass(PsiElement element) {
         return element != null && element.toString().startsWith(PSI_CLASS);
    }


    public static boolean isInject(PsiElement element) {
        return element != null &&
                element.toString().startsWith(JS_ES_6_DECORATOR) &&
                element.getText().startsWith(NG_INJECT);
    }


    public static boolean isTypeScriptClass(PsiElement element) {
        return element != null && element.toString().startsWith(TYPE_SCRIPT_CLASS);
    }

    public static boolean isTypeScriptParam(PsiElement element) {
        return element != null && element.toString().startsWith(TYPE_SCRIPT_PARAM);
    }

    public static boolean isJSBlock(PsiElement element) {
        return element != null && element.toString().startsWith(JS_BLOCK_STATEMENT);
    }

    public static Predicate<PsiElement> P_JSBlock() {
        return psiElement -> isJSBlock(psiElement);
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

    public static Optional<PsiElement> walkParent(PsiElement element, Function<PsiElement, Boolean> psiElementVisitor) {
        PsiElement walkElem = element;
        do {
            if (psiElementVisitor.apply(walkElem)) {
                return Optional.of(walkElem);
            }
            walkElem = walkElem.getParent();
        } while (walkElem != null);

        return Optional.empty();
    }

    public static List<PsiElement> walkParents(PsiElement element, Function<PsiElement, Boolean> psiElementVisitor) {
        PsiElement walkElem = element;
        List<PsiElement> retVal = new LinkedList<>();
        do {
            if (psiElementVisitor.apply(walkElem)) {
                retVal.add(walkElem);
            }
            walkElem = walkElem.getParent();
        } while (walkElem != null);

        return retVal;
    }

    public static List<PsiElementContext> walkParents(PsiElementContext element, Function<PsiElementContext, Boolean> psiElementVisitor) {
        PsiElementContext walkElem = element;
        List<PsiElementContext> retVal = new LinkedList<>();
        do {
            if (psiElementVisitor.apply(walkElem)) {
                retVal.add(walkElem);
            }
            walkElem = new PsiElementContext(walkElem.getElement().getParent());
        } while (walkElem.getElement() != null);

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

    public static boolean isRootNav(PsiElement el) {
        return (el.toString().equals(JS_CALL_EXPRESSION)) && el.getText().startsWith(JS_UIROUTER_MODULE_FOR_ROOT);
    }

    public static Stream<PsiElementContext> queryContent(PsiFile file, Object... items) {
        Stream<PsiElementContext> subItem = Arrays.asList(file).stream().map(item -> new PsiElementContext(item));
        return _query(subItem, items);
    }

    public static Stream<PsiElementContext> queryContent(PsiElement element, Object... items) {
        Stream<PsiElementContext> subItem = Arrays.asList(new PsiElementContext(element)).stream();
        return _query(subItem, items);
    }

    /**
     * A simple query facility to redude code
     * <p>
     * <p>
     * Syntax
     * <p>
     * query: command*
     * command: ElementType | SIMPLE_COMMAND | CONSUMER | PREDICATE | FUNCTION
     * <p>
     * ElementyType: char*
     * SIMPLE_COMMAND: > | :FIRST | TEXT:(<char *>) | TEXT*:(<char *>)
     * PREDICATE ...: Function as defined by Java
     *
     * @param subItem
     * @param items
     * @return
     */
    private static Stream<PsiElementContext> _query(Stream<PsiElementContext> subItem, Object[] items) {
        boolean directChild = false;
        for (Object item : items) {
            //shortcut. we can skip  processing at empty subitems

            if (item instanceof String) {
                subItem = privateDistinctEl(subItem); //lets reduce mem consumption by distincting the subset results

                final String text = (String) item;
                String subCommand = (text).trim();
                directChild = (text).startsWith(">");
                if (item instanceof String && (text).matches("^\\s*TEXT\\s*\\:\\s*\\((.*)\\)\\s*$")) {
                    Pattern p = Pattern.compile("^\\s*TEXT\\s*\\:\\s*\\((.*)\\)\\s*$");
                    subItem = subItem.filter(psiElementContext -> {

                        Matcher m = p.matcher(text);
                        if (!m.find()) {
                            return false;
                        }
                        String matchText = m.group(1);
                        boolean literalEquals = false;
                        if (matchText.matches("^[\\\"\\'](.*)[\\\"\\']$")) {
                            matchText = matchText.substring(1, matchText.length() - 1);
                            literalEquals = true;
                        }

                        return literalEquals ? literalEquals(psiElementContext.getText(), matchText) : matchText.equals(psiElementContext.getText());
                    });
                    continue;
                } else if (item instanceof String && (text).matches("^\\s*TEXT\\*\\s*\\:\\s*\\((.*)\\)\\s*$")) {
                    Pattern p = Pattern.compile("^\\s*TEXT\\*\\s*\\:\\s*\\((.*)\\)\\s*$");
                    subItem = subItem.filter(psiElementContext -> {

                        Matcher m = p.matcher(text);
                        if (!m.find()) {
                            return false;
                        }
                        String matchText = m.group(1);

                        return listeralStartsWith(psiElementContext.getText(), matchText) || psiElementContext.getText().startsWith(matchText);
                    });
                    continue;


                } else if (item instanceof String && (text).matches("^\\s*NAME\\s*\\:\\s*\\((.*)\\)\\s*$")) {
                    Pattern p = Pattern.compile("^\\s*NAME*\\:\\s*\\((.*)\\)\\s*$");
                    subItem = subItem.filter(psiElementContext -> {

                        Matcher m = p.matcher(text);
                        if (!m.find()) {
                            return false;
                        }
                        String matchText = m.group(1);
                        boolean literalEquals = false;
                        if (matchText.matches("^[\\\"\\'](.*)[\\\"\\']$")) {
                            matchText = matchText.substring(1, matchText.length() - 1);
                            literalEquals = true;
                        }

                        return literalEquals ? literalEquals(psiElementContext.getName(), matchText) : matchText.equals(psiElementContext.getName());
                    });
                    continue;
                } else if (item instanceof String && (text).matches("^\\s*NAME\\*\\s*\\:\\s*\\((.*)\\)\\s*$")) {
                    Pattern p = Pattern.compile("^\\s*NAME\\*\\s*\\:\\s*\\((.*)\\)\\s*$");
                    subItem = subItem.filter(psiElementContext -> {

                        Matcher m = p.matcher(text);
                        if (!m.find()) {
                            return false;
                        }
                        String matchText = m.group(1);

                        return listeralStartsWith(psiElementContext.getName(), matchText) ||

                                psiElementContext.getName().startsWith(matchText);
                    });
                    continue;


                } else if (directChild) {
                    subCommand = subCommand.substring(1).trim();
                    if (subCommand.trim().isEmpty()) {
                        //issued in sep query
                        continue;
                    } else {
                        directChild = false;
                    }
                }

                final String finalSubCommand = subCommand;
                if (subCommand.toLowerCase().equals(P_FIRST)) {
                    PsiElementContext firstItem = subItem.findFirst().orElse(null);
                    if (firstItem != null) {
                        subItem = Arrays.asList(firstItem).stream();
                    } else {
                        subItem = emptyStream();
                    }
                } else if (subCommand.startsWith("PARENTS:(")) {
                    Pattern p = Pattern.compile("^\\s*PARENTS\\s*\\:\\s*\\((.*)\\)\\s*$");
                    subItem = subItem.flatMap(theItem -> theItem.parents().stream().filter(psiElementContext -> {

                        Matcher m = p.matcher(text);
                        if (!m.find()) {
                            return false;
                        }
                        String matchText = m.group(1);

                        return listeralStartsWith(psiElementContext.getName(), matchText) || listeralStartsWith(psiElementContext.getElement().toString(), matchText);
                    }));
                } else if (subCommand.equals(P_PARENTS)) {
                    subItem = subItem.flatMap(theItem -> theItem.parents().stream());
                } else if (subCommand.equals(P_LAST)) {
                    Optional<PsiElementContext> reduced = subItem.reduce((theItem, theItem2) -> theItem2);
                    if (reduced.isPresent()) {
                        subItem = Arrays.asList(reduced.get()).stream();
                    } else {
                        subItem = Collections.<PsiElementContext>emptyList().stream();
                    }

                } else if (directChild) {
                    subItem = subItem.filter(psiItem -> psiItem.toString().startsWith(finalSubCommand));
                } else {
                    subItem = subItem.flatMap(psiItem -> psiItem.findPsiElements(psiElement ->  psiElement.toString().startsWith(finalSubCommand)).stream());
                }
            } else if (item instanceof Consumer) {
                subItem.forEach(elem -> {
                    ((Consumer) item).accept(elem);
                });
                subItem = emptyStream();
            } else if (item instanceof Predicate) {
                subItem = subItem.filter(elem -> ((Predicate) item).test(elem));
            } else if (item instanceof Function) {
                subItem = subItem.map(elem -> ((Function<PsiElementContext, PsiElementContext>) item).apply(elem));
            } else {
                throw new RuntimeException("Undefined query mapping");
            }
        }

        return privateDistinctEl(subItem);
    }

    static private Stream<PsiElementContext> privateDistinctEl(Stream<PsiElementContext> inStr) {
        final Set<PsiElementContext> elIdx = new HashSet<>();
        return inStr.filter(el -> {
            if (elIdx.contains(el)) {
                return false;
            } else {
                elIdx.add(el);
                return true;
            }
        });
    }

    private static <T> Stream<T> emptyStream() {
        return Collections.<T>emptyList().stream();
    }
}
