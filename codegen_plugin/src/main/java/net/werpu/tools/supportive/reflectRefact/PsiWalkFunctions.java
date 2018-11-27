package net.werpu.tools.supportive.reflectRefact;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.reflectRefact.navigation.BaseQueryEngineImplementation;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;
import net.werpu.tools.supportive.reflectRefact.navigation.PsiElementQueryAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.*;
import static net.werpu.tools.supportive.utils.IntellijUtils.flattendArr;


/**
 * this is the central hub for all code analysis
 * and refactoring
 * Basically a query facility in a jquery/css like manner
 * which eases the detection of code patterns significantly
 * It sits on top of Intellijs Psi functionality
 */
public class PsiWalkFunctions extends BaseQueryEngineImplementation<PsiElementContext> {

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
    public static final String JS_ES_6_FIELD_STATEMENT = "ES6FieldStatement";

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
    public static final Object[] TN_COMP_TRANSCLUDE = {TYPE_SCRIPT_FIELD, NAME_EQ("transclude")};
    public static final String PSI_ELEMENT_JS_STRING_TEMPLATE_PART = "PsiElement(JS:STRING_TEMPLATE_PART)";
    public static final String JS_PROPERTY = "JSProperty";
    public static final Object[] TN_COMP_BINDINGS = {TYPE_SCRIPT_FIELD, NAME_EQ("bindings"), JS_OBJECT_LITERAL_EXPRESSION, JS_PROPERTY};
    public static final String JS_ARRAY_LITERAL_EXPRESSION = "JSArrayLiteralExpression";
    public static final Object[] TN_COMP_CONTROLLER_ARR = {TYPE_SCRIPT_FIELD, NAME_EQ("controller"), JS_ARRAY_LITERAL_EXPRESSION};
    public static final String JS_ARGUMENTS_LIST = "JSArgumentList";
    public static final String JS_PARAMETER_BLOCK = "JSParameterBlock";

    public static final String JS_VAR_STATEMENT = "JSVarStatement";

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


    public static final Object[] TN_COMP_CONTROLLER_FUNC = {CHILD_ELEM, TYPESCRIPT_FUNCTION_EXPRESSION};
    public static final Object[] TN_COMP_PARAM_LISTS = {TN_COMP_CONTROLLER_ARR, TN_COMP_CONTROLLER_FUNC, CHILD_ELEM, TYPE_SCRIPT_PARAMETER_LIST, TYPE_SCRIPT_PARAM};
    public static final Object[] TN_COMP_STR_INJECTS = {CHILD_ELEM, JS_LITERAL_EXPRESSION, PSI_ELEMENT_JS_STRING_LITERAL};
    /*prdefined queries end*/


    /*Specific queries used by the transformations*/

    //TODO possible problem with multiple modules per file here
    public static final Object[] ANG1_MODULE_DCL = {JS_CALL_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("module"), PARENTS, JS_CALL_EXPRESSION};
    //module name starting from DCL
    public static final Object[] ANG1_MODULE_NAME = {JS_ARGUMENTS_LIST, PSI_ELEMENT_JS_STRING_LITERAL};
    //requires starting from DCL
    public static final Object[] ANG1_MODULE_REQUIRES = {JS_ARRAY_LITERAL_EXPRESSION, PSI_ELEMENT_JS_STRING_LITERAL};
    public static final Object TYPE_SCRIPT_SINGLE_TYPE = "TypeScriptSingleType";
    public static final Object JS_STRING_TEMPLATE_EXPRESSION = "JSStringTemplateExpression";


    public static Object[] DEF_CALL(String callType) {
        return new Object[]{JS_CALL_EXPRESSION, DIRECT_CHILD(PSI_ELEMENT_JS_IDENTIFIER), TEXT_EQ(callType)};
    }

    @NotNull
    public static Object[] TN_DEC_COMPONENT_NAME(String className) {
        return new Object[]{TYPE_SCRIPT_NEW_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER, NAME_EQ(className), PARENTS, JS_ARGUMENTS_LIST, PSI_ELEMENT_JS_STRING_LITERAL};
    }



    /*helpers end*/
    public static Stream<PsiElementContext> queryContent(PsiFile file, Object... items) {
        items = flattendArr(items).stream().toArray(Object[]::new);
        Stream<PsiElementContext> subItem = asList(file).stream().map(item -> new PsiElementContext(item));
        return execQuery(subItem, items);
    }

    public static Stream<PsiElementContext> queryContent(PsiElement element, Object... items) {
        items = flattendArr(items).stream().toArray(Object[]::new);
        Stream<PsiElementContext> subItem = asList(new PsiElementContext(element)).stream();
        return execQuery(subItem, items);
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



    public static List<PsiElementContext> walkParents(PsiElementContext element, Function<PsiElementContext, Boolean> psiElementVisitor) {
        PsiElementContext walkElem = element;
        List<PsiElementContext> retVal = new LinkedList<>();
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


    static {
        queryEngine = new TreeQueryEngine<PsiElementContext>(new PsiElementQueryAdapter());
    }


}
