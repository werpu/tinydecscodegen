package net.werpu.tools.supportive.reflectRefact;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static net.werpu.tools.supportive.utils.StringUtils.literalEquals;
import static net.werpu.tools.supportive.utils.StringUtils.literalStartsWith;

public class PsiWalkFunctions {

    private static final String ERR_UNDEFINED_QUERY_MAPPING = "Undefined query mapping";

    /*ElementTypes*/
    public static final String JS_PROP_TEMPLATE = "template";
    public static final String JS_PROP_TEMPLATE_URL = "templateUrl";
    public static final String JS_REFERENCE_EXPRESSION = "JSReferenceExpression";
    public static final String JS_EXPRESSION_STATEMENT = "JSExpressionStatement";
    public static final String JS_BLOCK_ELEMENT = "JSBlockElement";
    public static final String JS_DEFINITION_EXPRESSION = "JSDefinitionExpression";
    public static final String JS_BLOCK_STATEMENT = "JSBlockStatement";
    public static final String JS_OBJECT_LITERAL_EXPRESSION = "JSObjectLiteralExpression";
    public static final String JS_LITERAL_EXPRESSION = "JSLiteralExpression";
    public static final String JS_ES_6_DECORATOR = "ES6Decorator";
    public static final String JS_ES_6_FROM_CLAUSE = "ES6FromClause";
    public static final String NG_COMPONENT = "@Component";
    public static final String NG_INJECT = "@Inject";
    public static final String NG_CONFIG = "@Config";
    public static final String TN_CONFIG = NG_CONFIG;
    public static final String NG_CONTROLLER = "@Controller";
    public static final String TN_CONTROLLER = NG_CONTROLLER;
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
    public static final String JS_RETURN_STATEMENT = "JSReturnStatement";
    public static final String TN_UIROUTER_MODULE_FOR_ROOT = "TN_RootRouter";
    public static final String TN_ROUTES_UIROUTER_MODULE_FOR_ROOT = "TN_RoutesRootRouter";
    public static final String NPM_ROOT = "package.json";
    public static final String PSI_ELEMENT_JS_RBRACKET = "PsiElement(JS:RBRACKET)";
    public static final String PSI_ELEMENT_JS_IDENTIFIER = "PsiElement(JS:IDENTIFIER)";
    public static final String PSI_ELEMENT_JS_STRING_LITERAL = "PsiElement(JS:STRING_LITERAL)";
    public static final Object[] TN_COMP_CONTROLLER_AS = {TYPE_SCRIPT_FIELD, NAME_EQ("controllerAs"), PSI_ELEMENT_JS_STRING_LITERAL};
    public static final String PSI_ELEMENT_JS_STRING_TEMPLATE_PART = "PsiElement(JS:STRING_TEMPLATE_PART)";
    public static final String JS_PROPERTY = "JSProperty";
    public static final Object[] TN_COMP_BINDINGS = {TYPE_SCRIPT_FIELD, NAME_EQ("bindings"), JS_OBJECT_LITERAL_EXPRESSION, JS_PROPERTY};
    public static final String JS_ARRAY_LITERAL_EXPRESSION = "JSArrayLiteralExpression";
    public static final Object[] TN_COMP_CONTROLLER_ARR = {TYPE_SCRIPT_FIELD, NAME_EQ("controller"), JS_ARRAY_LITERAL_EXPRESSION};
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

    public static final String NG_MODULE = "@NgModule";
    /*prdefined queries*/
    public static final Object[] MODULE_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_MODULE), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] MODULE_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_MODULE), JS_ARGUMENTS_LIST};
    public static final Object[] MODULE_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_MODULE), PARENTS_EQ(TYPE_SCRIPT_CLASS)};

    public static final Object[] COMPONENT_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_COMPONENT), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] COMPONENT_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_COMPONENT), JS_ARGUMENTS_LIST};
    public static final Object[] COMPONENT_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_COMPONENT), PARENTS_EQ(TYPE_SCRIPT_CLASS)};

    public static final String NG_DIRECTIVE = "@Directive";
    public static final Object[] DIRECTIVE_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_DIRECTIVE), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] DIRECTIVE_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_DIRECTIVE), JS_ARGUMENTS_LIST};
    public static final Object[] DIRECTIVE_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_DIRECTIVE), PARENTS_EQ(TYPE_SCRIPT_CLASS)};


    public static final Object[] CONFIG_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_CONFIG), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] CONFIG_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_CONFIG), JS_ARGUMENTS_LIST};
    public static final Object[] CONFIG_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_CONFIG), PARENTS_EQ(TYPE_SCRIPT_CLASS)};


    public static final String NG_FILTER = "@Filter";
    public static final Object[] FILTER_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_FILTER), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] FILTER_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_FILTER), JS_ARGUMENTS_LIST};
    public static final Object[] FILTER_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_FILTER), PARENTS_EQ(TYPE_SCRIPT_CLASS)};

    public static final String NG_PIPE = "@Pipe";
    public static final Object[] PIPE_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_PIPE), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] PIPE_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_PIPE), JS_ARGUMENTS_LIST};
    public static final Object[] PIPE_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_PIPE), PARENTS_EQ(TYPE_SCRIPT_CLASS)};


    public static final String NG_INJECTABLE = "@Injectable";
    public static final Object[] SERVICE_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_INJECTABLE), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] SERVICE_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_INJECTABLE), JS_ARGUMENTS_LIST};
    public static final Object[] SERVICE_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_INJECTABLE), PARENTS_EQ(TYPE_SCRIPT_CLASS)};


    public static final Object[] CONTROLLER_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_CONTROLLER), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] CONTROLLER_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_CONTROLLER), JS_ARGUMENTS_LIST};
    public static final Object[] CONTROLLER_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_CONTROLLER), PARENTS_EQ(TYPE_SCRIPT_CLASS)};

    public static final String NG_DTO = "@Dto";
    public static final Object[] DTO_ANN = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_DTO), PARENTS_EQ(JS_ES_6_DECORATOR)};
    public static final Object[] DTO_ARGS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_DTO), JS_ARGUMENTS_LIST};
    public static final Object[] DTO_CLASS = {JS_ES_6_DECORATOR, TEXT_STARTS_WITH(NG_DTO), PARENTS_EQ(TYPE_SCRIPT_CLASS)};


    public static final String CHILD_ELEM = ">";
    public static final Object[] TN_COMP_CONTROLLER_FUNC = {CHILD_ELEM, TYPESCRIPT_FUNCTION_EXPRESSION};
    public static final Object[] TN_COMP_PARAM_LISTS = {TN_COMP_CONTROLLER_ARR, TN_COMP_CONTROLLER_FUNC, CHILD_ELEM, TYPE_SCRIPT_PARAMETER_LIST, TYPE_SCRIPT_PARAM};
    public static final Object[] TN_COMP_STR_INJECTS = {CHILD_ELEM, JS_LITERAL_EXPRESSION, PSI_ELEMENT_JS_STRING_LITERAL};
    /*prdefined queries end*/


    /*Specific queries used by the transformations*/

    public static final Object[] ANG1_MODULE_DCL = {JS_CALL_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("module"), P_PARENTS, JS_CALL_EXPRESSION};
    //module name starting from DCL
    public static final Object[] ANG1_MODULE_NAME = {JS_ARGUMENTS_LIST, PSI_ELEMENT_JS_STRING_LITERAL};
    //requires starting from DCL
    public static final Object[] ANG1_MODULE_REQUIRES = {JS_ARRAY_LITERAL_EXPRESSION, PSI_ELEMENT_JS_STRING_LITERAL};
    public static final Object TYPE_SCRIPT_SINGLE_TYPE = "TypeScriptSingleType";
    public static final Object JS_STRING_TEMPLATE_EXPRESSION = "JSStringTemplateExpression";




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

    @NotNull
    public static Object[] TN_DEC_COMPONENT_NAME(String className) {
        return new Object[]{TYPE_SCRIPT_NEW_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER, NAME_EQ(className), PARENTS, JS_ARGUMENTS_LIST, PSI_ELEMENT_JS_STRING_LITERAL};
    }
    /*helpers end*/


    /*predefined rex for deeper string analysis*/
    static final String RE_TEXT_EQ = "^\\s*TEXT\\s*\\:\\s*\\((.*)\\)\\s*$";
    static final String RE_TEXT_STARTS_WITH = "^\\s*TEXT\\*\\s*\\:\\s*\\((.*)\\)\\s*$";
    static final String RE_NAME_EQ = "^\\s*NAME\\s*\\:\\s*\\((.*)\\)\\s*$";
    static final String RE_STRING_LITERAL = "^[\\\"\\'](.*)[\\\"\\']$";
    static final String RE_NAME_STARTS_WITH = "^\\s*NAME\\*\\s*\\:\\s*\\((.*)\\)\\s*$";
    static final String RE_PARENTS_EQ = "^\\s*PARENTS\\s*\\:\\s*\\((.*)\\)\\s*$";


    public static boolean inTemplateHolder(PsiElement element) {

        return concat(concat(queryContent(element, COMPONENT_CLASS),
                queryContent(element, DIRECTIVE_CLASS)),
                queryContent(element, CONTROLLER_CLASS)).findFirst()
                .isPresent();

    }

    public static boolean inTemplateHolder(PsiElementContext element) {
        PsiElement element1 = element.getElement();

        return inTemplateHolder(element1);
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
     * This is the central method of our query engine
     * it follows a very simple grammar which distincts
     * between string matches and simple commands and function
     * matches
     * <p>
     * It allows to trace down a tree css/jquery style but also
     * allows simple  parent backtracking
     * this one walks up all parents which match the subsequent criteria
     * once this is done, if you want to avoid that behavior
     * you either can use a firstElem() or reduce
     * or as shortcut if you do not want to leave the query
     * P_FIRST (:FIRST)
     * or
     * P_LAST (:LAST)
     * <p>
     * to make the reduction within the query
     * <p>
     * Syntax
     * <p>
     * QUERY: COMMAND*
     *      COMMAND: ELEMENT_TYPE | SIMPLE_COMMAND | FUNC
     *
     *          ELEMENT_TYPE: char*
     *
     *          SIMPLE_COMMAND: > | :FIRST | TEXT:(<char *>) | TEXT*:(<char *>) | NAME:(<char *>) | NAME*:(<char *>) | PARENTS:(<char *> | ElementType) | PARENTS:  | :LAST | :FIRST
     *              PARENTS:(COMMAND) shortcut for PARENTS:, TEXT:(...) or :PARENTS,COMMAND
     *
     *          FUNC: CONSUMER | PREDICATE | FUNCTION
     *              CONSUMER: Function as defined by Java
     *              PREDICATE: Function as defined by Java
     *              FUNCTION: Function as defined by Java
     *
     * @param subItem item to be queried
     * @param commands the list of commands to be processed
     * @return
     */
    private static Stream<PsiElementContext> execQuery(Stream<PsiElementContext> subItem, Object[] commands) {


        for (Object command : commands) {
            if (isStringElement(command)) {
                //lets reduce mem consumption by distincting the subset results
                subItem = subItem.distinct();

                String strCommand = ((String) command).trim();
                SIMPLE_COMMAND simpleCommand = SIMPLE_COMMAND.fromValue(strCommand);
                if (simpleCommand != null) {//command found
                    switch (simpleCommand) {
                        case CHILD_ELEM:
                            subItem = subItem.flatMap(theItem -> theItem.getChildren(el -> Boolean.TRUE).stream());
                            continue;
                        case RE_TEXT_EQ:
                            subItem = handleTextEQ(subItem, strCommand);
                            continue;
                        case RE_TEXT_STARTS_WITH:
                            subItem = handleTextStartsWith(subItem, strCommand);
                            continue;
                        case RE_NAME_EQ:
                            subItem = handleNameEQ(subItem, strCommand);
                            continue;
                        case RE_NAME_STARTS_WITH:
                            subItem = handleNameStartsWith(subItem, strCommand);
                            continue;
                        case RE_PARENTS_EQ:
                            subItem = handleParentsEq(subItem, strCommand);
                            continue;
                        case P_FIRST:
                            subItem = handlePFirst(subItem);
                            continue;
                        case P_PARENTS:
                            subItem = parentsOf(subItem);
                            continue;
                        case P_LAST:
                            subItem = handlePLast(subItem);
                            continue;
                    }
                }
                subItem = elementTypeMatch(subItem, strCommand);
                continue;

            }
            subItem = functionTokenMatch(subItem, command);
        }
        return subItem.distinct();
    }

    @NotNull
    private static Stream<PsiElementContext> parentsOf(Stream<PsiElementContext> subItem) {
        return subItem.flatMap(theItem -> theItem.parents().stream());
    }

    /**
     * resolve an incoming function
     * from the query chain
     * <p>
     * Depending on the type you can use the passing of functions for various things
     * <li>Consumer&lt;PsiElementContext&gt;... to extract values from the current query position
     * in this case the returned stream is the same as the one before</li>
     * <li>Predicate&lt;PsiElementContext&gt;... to filter items out, in this case
     * the returned stream is the filtered stream of the old one</li>
     * <li>Function&lt;PsiElementContext, PsiElementContext&gt;</li>
     *
     * @param subItem       the subitem to be resolved
     * @param func a function of Consumer, Predicate, or (Function<PsiElementContext, PsiElementContext>)
     * @return the processed subitem
     */
    @NotNull
    private static Stream<PsiElementContext> functionTokenMatch(Stream<PsiElementContext> subItem, Object func) {
        if (func instanceof Consumer) {
            subItem = handleConsumer(subItem, (Consumer) func);
        } else if (func instanceof Predicate) {
            subItem = handlePredicate(subItem, (Predicate) func);
        } else if (func instanceof Function) {
            subItem = handleFunction(subItem, (Function<PsiElementContext, PsiElementContext>) func);
        } else {
            throw new IllegalArgumentException(ERR_UNDEFINED_QUERY_MAPPING);
        }
        return subItem;
    }

    private static boolean isStringElement(Object item) {
        return item instanceof String;
    }


    @NotNull
    private static Stream<PsiElementContext> elementTypeMatch(Stream<PsiElementContext> subItem, String finalSubCommand) {
        subItem = subItem.flatMap(psiItem -> psiItem.findPsiElements(psiElement -> {
            String cmdString = psiElement.toString();
            return cmdString.equalsIgnoreCase(finalSubCommand) || cmdString.startsWith(finalSubCommand + ":");
        }).stream())
                .distinct()
                .collect(Collectors.toList()).stream();
        return subItem;
    }


    @NotNull
    private static Stream<PsiElementContext> handleFunction(Stream<PsiElementContext> subItem, Function<PsiElementContext, PsiElementContext> item) {
        subItem = subItem.map(item::apply);
        return subItem;
    }

    @NotNull
    private static Stream<PsiElementContext> handlePredicate(Stream<PsiElementContext> subItem, Predicate item) {
        subItem = subItem.filter(item::test);
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
        Pattern p = Pattern.compile(RE_TEXT_STARTS_WITH);
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
        Pattern p = Pattern.compile(RE_TEXT_EQ);
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

    private static <T> Stream<T> emptyStream() {
        return Collections.<T>emptyList().stream();
    }


}
