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

    /*ElementTypes*/
    public static final String JS_PROP_TEMPLATE = "template";
    public static final String JS_PROP_TEMPLATE_URL = "templateUrl";
    public static final String NG_TYPE_COMPONENT = "Component";
    public static final String NG_TYPE_DIRECTIVE = "Directive";
    public static final String NG_TYPE_CONTROLLER = "Controller";
    public static final String JS_REFERENCE_EXPRESSION = "JSReferenceExpression";
    public static final String JS_EXPRESSION_STATEMENT = "JSExpressionStatement";
    public static final String JS_BLOCK_ELEMENT = "JSBlockElement";
    public static final String JS_BLOCK_STATEMENT = "JSBlockStatement";
    public static final String JS_OBJECT_LITERAL_EXPRESSION = "JSObjectLiteralExpression";
    public static final String JS_LITERAL_EXPRESSION = "JSLiteralExpression";
    public static final String JS_ES_6_DECORATOR = "ES6Decorator";
    public static final String JS_ES_6_FROM_CLAUSE = "ES6FromClause";
    public static final String NG_COMPONENT = "@Component";
    public static final String NG_INJECT = "@Inject";
    public static final String TN_CONFIG = "@Config";
    public static final String TN_CONTROLLER = "@Controller";
    public static final String TYPE_SCRIPT_CLASS = "TypeScriptClass";
    public static final String TYPE_SCRIPT_FIELD = "TypeScriptField";
    public static final String TYPE_SCRIPT_NEW_EXPRESSION = "TypeScriptNewExpression";
    public static final String TYPE_SCRIPT_VARIABLE = "TypeScriptVariable";
    public static final String TYPE_SCRIPT_PARAM = "TypeScriptParameter";
    public static final String TYPE_SCRIPT_FUNC = "TypeScriptFunction";
    public static final String TYPE_SCRIPT_FUNC_EXPR = "TypeScriptFunctionExpression";
    public static final String TYPE_SCRIPT_PARAMETER_LIST = "TypeScriptParameterList";
    public static final String PSI_METHOD = "PsiMethod";
    public static final String TYPESCRIPT_FUNCTION_EXPRESSION = "TypeScriptFunctionExpression";
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
    public static final String PSI_ELEMENT_JS_STRING_TEMPLATE_PART = "PsiElement(JS:STRING_TEMPLATE_PART)";
    public static final String JS_PROPERTY = "JSProperty";
    public static final String JS_ARRAY_LITERAL_EXPRESSION = "JSArrayLiteralExpression";
    public static final String JS_ARGUMENTS_LIST = "JSArgumentList";
    public static final String JS_PARAMETER_BLOCK = "JSParameterBlock";

    public static final String JS_VAR_STATEMENT = "JSVarStatement";
    public static final String P_PARENTS = ":PARENTS";
    public static final String PARENTS = "PARENTS:";
    public static final String P_LAST = ":LAST";
    public static final String P_FIRST = ":first";

    public static final String PSI_CLASS = "PsiClass:";
    public static final String STRING_TEMPLATE_EXPR = "JSStringTemplateExpression";
    /*ElementTypes end*/

    //TODO LOGICAL OPS
    public static final String L_PAR = "(";
    public static final String R_PAR = ")";
    public static final String L_AND = "AND";
    public static final String L_OR = "OR";


    /*prdefined queries*/
    public static final Object[] MODULE_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@NgModule"), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] MODULE_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@NgModule"), JS_ARGUMENTS_LIST};
    public static final Object[] MODULE_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@NgModule"), PARENTS_EQ(TYPE_SCRIPT_CLASS)};

    public static final Object[] COMPONENT_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Component"), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] COMPONENT_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Component"), JS_ARGUMENTS_LIST};
    public static final Object[] COMPONENT_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Component"), PARENTS_EQ(TYPE_SCRIPT_CLASS)};

    public static final Object[] FILTER_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Filter"), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] FILTER_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Filter"), JS_ARGUMENTS_LIST};
    public static final Object[] FILTER_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Filter"), PARENTS_EQ(TYPE_SCRIPT_CLASS)};

    public static final Object[] PIPE_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Pipe"), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] PIPE_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Pipe"), JS_ARGUMENTS_LIST};
    public static final Object[] PIPE_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Pipe"), PARENTS_EQ(TYPE_SCRIPT_CLASS)};


    public static final Object[] SERVICE_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Injectable"), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] SERVICE_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Injectable"), JS_ARGUMENTS_LIST};
    public static final Object[] SERVICE_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Injectable"), PARENTS_EQ(TYPE_SCRIPT_CLASS)};


    public static final Object[] CONTROLLER_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Controller"), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] CONTROLLER_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Controller"), JS_ARGUMENTS_LIST};
    public static final Object[] CONTROLLER_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Controller"), PARENTS_EQ(TYPE_SCRIPT_CLASS)};

    public static final Object[] DTO_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Dto"), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] DTO_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Dto"), JS_ARGUMENTS_LIST};
    public static final Object[] DTO_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH("@Dto"), PARENTS_EQ(TYPE_SCRIPT_CLASS)};



    public static final String CHILD_ELEM = ">";
    /*prdefined queries end*/


    /*Specific queries used by the transformations*/

    public static final Object[] ANG1_MODULE_DCL = {JS_CALL_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("module"), P_PARENTS, JS_CALL_EXPRESSION};
    //module name starting from DCL
    public static final Object[] ANG1_MODULE_NAME = {JS_ARGUMENTS_LIST, PSI_ELEMENT_JS_STRING_LITERAL};
    //requires starting from DCL
    public static final Object[] ANG1_MODULE_REQUIRES = {JS_ARRAY_LITERAL_EXPRESSION, PSI_ELEMENT_JS_STRING_LITERAL};


    /*helpers*/
    public static String PARENTS_EQ(String val) {
        return "PARENTS:(" + val + ")";
    }

    public static String TEXT_EQ(String val) {
        return "TEXT:(" + val + ")";
    }

    public static String NAME_EQ(String val) {
        return "NAME:(" + val + ")";
    }

    public static String TEXT_STARTS_WITH(String val) {
        return "TEXT*:(" + val + ")";
    }
    public static String DIRECT_CHILD(String val) {
        return CHILD_ELEM + val;
    }

    public static Object[] DEF_CALL(String callType) {
        return new Object[]{JS_CALL_EXPRESSION, DIRECT_CHILD(PSI_ELEMENT_JS_IDENTIFIER), TEXT_EQ(callType)};
    }
    /*helpers end*/


    /*predefined rex for deeper string analysis*/
    private static final String RE_TEXT_EQ = "^\\s*TEXT\\s*\\:\\s*\\((.*)\\)\\s*$";
    private static final String RE_TEXT_STARTS_WITH = "^\\s*TEXT\\*\\s*\\:\\s*\\((.*)\\)\\s*$";
    private static final String RE_NAME_EQ = "^\\s*NAME\\s*\\:\\s*\\((.*)\\)\\s*$";
    private static final String RE_STRING_LITERAL = "^[\\\"\\'](.*)[\\\"\\']$";
    private static final String RE_NAME_STARTS_WITH = "^\\s*NAME\\*\\s*\\:\\s*\\((.*)\\)\\s*$";
    private static final String RE_PARENTS_EQ = "^\\s*PARENTS\\s*\\:\\s*\\((.*)\\)\\s*$";


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
                && (
                    getText(element).equals(JS_PROP_TEMPLATE) ||
                    getText(element).equals(JS_PROP_TEMPLATE_URL)
                )
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
        return execQuery(subItem, items);
    }

    public static Stream<PsiElementContext> queryContent(PsiElement element, Object... items) {
        Stream<PsiElementContext> subItem = Arrays.asList(new PsiElementContext(element)).stream();
        return execQuery(subItem, items);
    }

    /**
     * A simple query facility to reduce code
     * <p>
     * <p>
     * Syntax
     * <p>
     * query: command*
     * command: ElementType | SIMPLE_COMMAND | CONSUMER | PREDICATE | FUNCTION
     * <p>
     * ElementyType: char*
     * SIMPLE_COMMAND: > | :FIRST | TEXT:(<char *>) | TEXT*:(<char *>) | NAME:(<char *>) | NAME*:(<char *>) | PARENTS:(<char *> | ElementType) | PARENTS:  | :LAST | :FIRST
     * PREDICATE ...: Function as defined by Java
     * <p>
     * TODO add contextual grammar info
     * PARENTS:(<char *> | ElementType) shortcut for PARENTS:, TEXT:(...) or :PARENTS,<ElementType>
     *
     * @param subItem
     * @param items
     * @return
     */
    private static Stream<PsiElementContext> execQuery(Stream<PsiElementContext> subItem, Object[] items) {
        boolean directChild;
        for (Object item : items) {
            //shortcut. we can skip  processing at empty subitems

            if (item instanceof String) {
                subItem = privateDistinctEl(subItem); //lets reduce mem consumption by distincting the subset results

                final String text = (String) item;
                String subCommand = (text).trim();
                directChild = (text).startsWith(CHILD_ELEM);
                if (item instanceof String && (text).matches(RE_TEXT_EQ)) {
                    subItem = handleTextEQ(subItem, text);
                    continue;
                } else if (item instanceof String && (text).matches(RE_TEXT_STARTS_WITH)) {
                    subItem = handleTextStartsWith(subItem, text);
                    continue;


                } else if (item instanceof String && (text).matches(RE_NAME_EQ)) {
                    subItem = handleNameEQ(subItem, text);
                    continue;
                } else if (item instanceof String && (text).matches(RE_NAME_STARTS_WITH)) {
                    subItem = handleNameStartsWith(subItem, text);
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
                if (subCommand.equals(P_FIRST)) {
                    subItem = handlePFirst(subItem);
                } else if (subCommand.startsWith("PARENTS:(")) {
                    subItem = handleParentsEq(subItem, text);
                } else if (subCommand.equals(P_PARENTS)) {
                    subItem = subItem.flatMap(theItem -> theItem.parents().stream());
                } else if (subCommand.equals(P_LAST)) {
                    subItem = handlePLast(subItem);

                } else if (directChild) {
                        subItem = handleDirectChild(subItem, finalSubCommand);
                } else {
                    subItem = handleFindSubItem(subItem, finalSubCommand);
                }
            } else if (item instanceof Consumer) {
                subItem = handleConsumer(subItem, (Consumer) item);
            } else if (item instanceof Predicate) {
                subItem = handlePredicate(subItem, (Predicate) item);
            } else if (item instanceof Function) {
                subItem = handleFunction(subItem, (Function<PsiElementContext, PsiElementContext>) item);
            } else {
                throw new RuntimeException("Undefined query mapping");
            }
        }

        return privateDistinctEl(subItem);
    }

    @NotNull
    private static Stream<PsiElementContext> handleFindSubItem(Stream<PsiElementContext> subItem, String finalSubCommand) {
        subItem = subItem.flatMap(psiItem -> psiItem.findPsiElements(psiElement -> psiElement.toString().startsWith(finalSubCommand)).stream());
        return subItem;
    }

    @NotNull
    private static Stream<PsiElementContext> handleDirectChild(Stream<PsiElementContext> subItem, String finalSubCommand) {
        subItem = subItem.filter(psiItem -> psiItem.toString().startsWith(finalSubCommand));
        return subItem;
    }

    @NotNull
    private static Stream<PsiElementContext> handleFunction(Stream<PsiElementContext> subItem, Function<PsiElementContext, PsiElementContext> item) {
        subItem = subItem.map(elem -> item.apply(elem));
        return subItem;
    }

    @NotNull
    private static Stream<PsiElementContext> handlePredicate(Stream<PsiElementContext> subItem, Predicate item) {
        subItem = subItem.filter(elem -> item.test(elem));
        return subItem;
    }

    @NotNull
    private static Stream<PsiElementContext> handleConsumer(Stream<PsiElementContext> subItem, Consumer item) {
        subItem.forEach(elem -> {
            item.accept(elem);
        });
        subItem = emptyStream();
        return subItem;
    }

    @NotNull
    private static Stream<PsiElementContext> handlePLast(Stream<PsiElementContext> subItem) {
        Optional<PsiElementContext> reduced = subItem.reduce((theItem, theItem2) -> theItem2);
        if (reduced.isPresent()) {
            subItem = Arrays.asList(reduced.get()).stream();
        } else {
            subItem = Collections.<PsiElementContext>emptyList().stream();
        }
        return subItem;
    }

    @NotNull
    private static Stream<PsiElementContext> handleParentsEq(Stream<PsiElementContext> subItem, String text) {
        Pattern p = Pattern.compile(RE_PARENTS_EQ);
        subItem = subItem.flatMap(theItem -> theItem.parents().stream().filter(psiElementContext -> {

            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
            String matchText = m.group(1);

            return literalStartsWith(psiElementContext.getName(), matchText) || literalStartsWith(psiElementContext.getElement().toString(), matchText);
        }));
        return subItem;
    }

    @NotNull
    private static Stream<PsiElementContext> handlePFirst(Stream<PsiElementContext> subItem) {
        PsiElementContext firstItem = subItem.findFirst().orElse(null);
        if (firstItem != null) {
            subItem = Arrays.asList(firstItem).stream();
        } else {
            subItem = emptyStream();
        }
        return subItem;
    }

    @NotNull
    private static Stream<PsiElementContext> handleNameStartsWith(Stream<PsiElementContext> subItem, String text) {
        Pattern p = Pattern.compile(RE_NAME_STARTS_WITH);
        subItem = subItem.filter(psiElementContext -> {

            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
            String matchText = m.group(1);

            return literalStartsWith(psiElementContext.getName(), matchText) ||

                    psiElementContext.getName().startsWith(matchText);
        });
        return subItem;
    }

    @NotNull
    private static Stream<PsiElementContext> handleNameEQ(Stream<PsiElementContext> subItem, String text) {
        Pattern p = Pattern.compile(RE_NAME_EQ);
        subItem = subItem.filter(psiElementContext -> {

            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
            String matchText = m.group(1);
            boolean literalEquals = false;
            if (matchText.matches(RE_STRING_LITERAL)) {
                matchText = matchText.substring(1, matchText.length() - 1);
                literalEquals = true;
            }

            return literalEquals ? literalEquals(psiElementContext.getName(), matchText) : matchText.equals(psiElementContext.getName());
        });
        return subItem;
    }

    @NotNull
    private static Stream<PsiElementContext> handleTextStartsWith(Stream<PsiElementContext> subItem, String text) {
        Pattern p = Pattern.compile(PsiWalkFunctions.RE_TEXT_STARTS_WITH);
        subItem = subItem.filter(psiElementContext -> {

            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
            String matchText = m.group(1);

            return literalStartsWith(psiElementContext.getText(), matchText) || psiElementContext.getText().startsWith(matchText);
        });
        return subItem;
    }

    @NotNull
    private static Stream<PsiElementContext> handleTextEQ(Stream<PsiElementContext> subItem, String text) {
        Pattern p = Pattern.compile(PsiWalkFunctions.RE_TEXT_EQ);
        subItem = subItem.filter(psiElementContext -> {

            Matcher m = p.matcher(text);
            if (!m.find()) {
                return false;
            }
            String matchText = m.group(1);
            boolean literalEquals = false;
            if (matchText.matches(RE_STRING_LITERAL)) {
                matchText = matchText.substring(1, matchText.length() - 1);
                literalEquals = true;
            }

            return literalEquals ? literalEquals(psiElementContext.getText(), matchText) : matchText.equals(psiElementContext.getText());
        });
        return subItem;
    }

    @NotNull
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
