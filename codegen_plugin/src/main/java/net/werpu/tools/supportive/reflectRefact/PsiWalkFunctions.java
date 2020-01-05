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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.reflectRefact.navigation.BaseQueryEngineImplementation;
import net.werpu.tools.supportive.reflectRefact.navigation.PsiElementQueryAdapter;
import net.werpu.tools.supportive.reflectRefact.navigation.QueryExtension;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;
import net.werpu.tools.supportive.reflectRefact.navigation.ideafixes.PsiRecursiveElementWalkingVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
    public static final String JS_DOC_COMMENT = "JSDocComment";
    /*ElementTypes*/
    public static final String JS_PROP_TEMPLATE = "template";
    public static final String JS_PROP_TEMPLATE_URL = "templateUrl";
    public static final String JS_REFERENCE_EXPRESSION = "JSReferenceExpression";
    public static final String JS_EXPRESSION_STATEMENT = "JSExpressionStatement";
    public static final String JS_BLOCK_ELEMENT = "JSBlockElement";
    public static final String JS_DEFINITION_EXPRESSION = "JSDefinitionExpression";
    public static final String JS_BLOCK_STATEMENT = "JSBlockStatement";
    public static final String JS_OBJECT_LITERAL_EXPRESSION = "JSObjectLiteralExpression";
    public static final String JS_ASSIGNMENT_EXPRESSION = "JSAssignmentExpression";
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
    public static final String TYPESCRIPT_IMPORT_STATEMENT = "TypeScriptImportStatement";
    public static final String TYPE_SCRIPT_VARIABLE = "TypeScriptVariable";
    public static final String TYPE_SCRIPT_PARAM = "TypeScriptParameter";
    public static final String TYPE_SCRIPT_FUNC = "TypeScriptFunction";
    public static final String TYPE_SCRIPT_FUNC_EXPR = "TypeScriptFunctionExpression";
    public static final String TYPE_SCRIPT_PARAMETER_LIST = "TypeScriptParameterList";
    public static final String PSI_METHOD = "PsiMethod";
    public static final String TYPESCRIPT_FUNCTION_EXPRESSION = "TypeScriptFunctionExpression";
    public static final String JS_ES_6_IMPORT_DECLARATION = "ES6ImportDeclaration";
    public static final QueryExtension<Object> ANY_TS_IMPORT = ALL(SUB_QUERY(JS_ES_6_IMPORT_DECLARATION), SUB_QUERY(TYPESCRIPT_IMPORT_STATEMENT));
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
    public static final String TN_DEC_PRJ_MARKER = "_marker.tn_dec_project";
    public static final String NG_PRJ_MARKER = "_marker.ng_project";
    public static final String TS_CONFIG = "tsconfig.json";
    public static final String PACKAGE_LOCK = "package-lock.json";
    public static final String PSI_ELEMENT_JS_RBRACKET = "PsiElement(JS:RBRACKET)";
    public static final String PSI_ELEMENT_JS_IDENTIFIER = "PsiElement(JS:IDENTIFIER)";
    public static final String PSI_ELEMENT_JS_STRING_LITERAL = "PsiElement(JS:STRING_LITERAL)";
    public static final String PSI_ELEMENT_JS_NUMERIC_LITERAL = "PsiElement(JS:NUMERIC_LITERAL)";
    public static final Object[] TN_COMP_CONTROLLER_AS = {TYPE_SCRIPT_FIELD, NAME_EQ("controllerAs"), PSI_ELEMENT_JS_STRING_LITERAL};
    public static final Object[] TN_COMP_SELECTOR = {TYPE_SCRIPT_FIELD, NAME_EQ("selector"), PSI_ELEMENT_JS_STRING_LITERAL};
    public static final Object[] TN_COMP_BIND_TO_CONTROLLER = {TYPE_SCRIPT_FIELD, NAME_EQ("bindToController"), PSI_ELEMENT_JS_STRING_LITERAL};
    public static final Object[] TN_COMP_BIND_RESTRICT = {TYPE_SCRIPT_FIELD, NAME_EQ("restrict"), PSI_ELEMENT_JS_STRING_LITERAL};
    public static final Object[] TN_COMP_TRANSCLUDE = {TYPE_SCRIPT_FIELD, NAME_EQ("transclude")};
    public static final Object[] TN_COMP_PRIORITY = {TYPE_SCRIPT_FIELD, NAME_EQ("priority")};
    public static final String PSI_ELEMENT_JS_STRING_TEMPLATE_PART = "PsiElement(JS:STRING_TEMPLATE_PART)";
    public static final String JS_PROPERTY = "JSProperty";
    public static final Object[] TN_COMP_BINDINGS = {TYPE_SCRIPT_FIELD, NAME_EQ("bindings"), JS_OBJECT_LITERAL_EXPRESSION, JS_PROPERTY};
    public static final Object[] TN_DIRECTIVE_BINDINGS = {JS_PROPERTY, NAME_EQ("scope"), FIRST, CHILD_ELEM, JS_OBJECT_LITERAL_EXPRESSION, CHILD_ELEM, JS_PROPERTY};
    public static final String JS_ARRAY_LITERAL_EXPRESSION = "JSArrayLiteralExpression";
    public static final Object[] TN_COMP_CONTROLLER_ARR = {TYPE_SCRIPT_FIELD, NAME_EQ("controller"), JS_ARRAY_LITERAL_EXPRESSION, FIRST};
    public static final Object[] TN_COMP_CONTROLLER_FUNC_ARR = {JS_PROPERTY, NAME_EQ("controller"), JS_ARRAY_LITERAL_EXPRESSION, FIRST};
    public static final String JS_ARGUMENTS_LIST = "JSArgumentList";
    public static final String JS_PARAMETER_BLOCK = "JSParameterBlock";
    public static final String JS_VAR_STATEMENT = "JSVarStatement";
    public static final String PSI_CLASS = "PsiClass:";
    public static final String STRING_TEMPLATE_EXPR = "JSStringTemplateExpression";
    public static final String XML_TEXT = "XmlText";
    public static final String XML_ATTRIBUTE_VALUE = "XmlToken:XML_ATTRIBUTE_VALUE_TOKEN";
    public static final String XML_ATTRIBUTE_NAME = "XmlToken:XML_NAME";
    public static final String PSI_XML_ATTRIBUTE = "PsiElement(XML_ATTRIBUTE)";
    public static final String PSI_XML_ATTRIBUTE_VALUE = "PsiElement(XML_ATTRIBUTE_VALUE)";
    //JSON
    public static final String JSON_PROPERTY = "JsonProperty";
    public static final String JSON_STRING_LITERAL = "JsonStringLiteral";
    public static final String JSON_OBJECT = "JsonObject";
    //ANGULAR specific types (new stuff), we reuse some angular2 stuff for angular 1 as well
    //this hackis but it works sufficiently
    public static final String ANGULAR_INTERPOLATION = "Angular2Interpolation";
    public static final String ANGULAR_PIPE = "Angular2PipeExpression";
    public static final String ANGULAR_FILTER = "AngularJSFilterExpression";
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
    public static final Object[] TN_COMP_PARAM_LISTS = {ANY(TN_COMP_CONTROLLER_ARR, TN_COMP_CONTROLLER_FUNC_ARR), TN_COMP_CONTROLLER_FUNC, CHILD_ELEM, TYPE_SCRIPT_PARAMETER_LIST, TYPE_SCRIPT_PARAM};
    public static final Object[] TN_COMP_STR_INJECTS = {CHILD_ELEM, JS_LITERAL_EXPRESSION, CHILD_ELEM, PSI_ELEMENT_JS_STRING_LITERAL};
    /*Specific queries used by the transformations*/
    //TODO possible problem with multiple modules per file here
    public static final Object[] ANG1_MODULE_DCL = {JS_CALL_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("module"), PARENTS, JS_CALL_EXPRESSION};
    /*prdefined queries end*/
    //module name starting from DCL
    public static final Object[] ANG1_MODULE_NAME = {JS_ARGUMENTS_LIST, PSI_ELEMENT_JS_STRING_LITERAL};
    //requires starting from DCL
    public static final Object[] ANG1_MODULE_REQUIRES = {JS_ARRAY_LITERAL_EXPRESSION, PSI_ELEMENT_JS_STRING_LITERAL};
    public static final String TYPE_SCRIPT_SINGLE_TYPE = "TypeScriptSingleType";
    public static final String JS_STRING_TEMPLATE_EXPRESSION = "JSStringTemplateExpression";
    private static final String ERR_UNDEFINED_QUERY_MAPPING = "Undefined query mapping";

    static {
        queryEngine = new TreeQueryEngine<PsiElementContext>(new PsiElementQueryAdapter());
    }

    public static Object[] ANNOTATED_TEMPLATE(String referenceType) {
        return new Object[]{JS_PROPERTY, PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("template"), PARENT, referenceType};
    }

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

    public static List<PsiElementContext> walkNextSiblings(PsiElementContext element, Function<PsiElementContext, Boolean> psiElementVisitor) {
        PsiElementContext walkElem = element;
        List<PsiElementContext> retVal = new LinkedList<>();
        PsiElement sibling = element.getElement().getNextSibling();

        while (sibling != null) {
            if (psiElementVisitor.apply(new PsiElementContext(sibling))) {
                retVal.add(new PsiElementContext(sibling));
            }
            sibling = sibling.getNextSibling();
        }
        while (sibling != null) ;

        return retVal;
    }

    public static List<PsiElementContext> walkPrevSibling(PsiElementContext element, Function<PsiElementContext, Boolean> psiElementVisitor) {
        PsiElementContext walkElem = element;
        List<PsiElementContext> retVal = new LinkedList<>();
        PsiElement sibling = element.getElement().getPrevSibling();

        while (sibling != null) {
            if (psiElementVisitor.apply(new PsiElementContext(sibling))) {
                retVal.add(new PsiElementContext(sibling));
            }
            sibling = sibling.getPrevSibling();
        }
        while (sibling != null) ;

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
                }
                super.visitElement(element);
            }
        };
    }

    public static boolean isRootNav(PsiElement el) {
        return (el.toString().equals(JS_CALL_EXPRESSION)) && el.getText().startsWith(JS_UIROUTER_MODULE_FOR_ROOT);
    }

    public static Object[] SUB_QUERY(Object... str1) {
        return str1;
    }
}
