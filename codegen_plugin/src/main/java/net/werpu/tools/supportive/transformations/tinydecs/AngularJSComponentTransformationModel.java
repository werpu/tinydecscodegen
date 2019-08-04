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

package net.werpu.tools.supportive.transformations.tinydecs;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;
import net.werpu.tools.supportive.refactor.IRefactorUnit;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;
import net.werpu.tools.supportive.transformations.shared.ITransformationModel;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.*;
import net.werpu.tools.supportive.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.*;
import static net.werpu.tools.supportive.utils.StringUtils.isThis;


@Getter
public class AngularJSComponentTransformationModel extends TypescriptFileContext implements ITransformationModel {

    /**
     * higher level queries only
     * used for the time being for the components
     */

    /**
     * searches for all inline functions within a certain function
     * the root for this query is the function definition block
     */
    public static final Object[] INLINE_FUNC_DECL = {TYPE_SCRIPT_FUNC_EXPR, TreeQueryEngine.PARENTS_EQ_FIRST(JS_EXPRESSION_STATEMENT)};
    /**
     * fetches all class attributes from a given class root is the class block
     */
    public static final Object[] CLASS_ATTRS = {CHILD_ELEM, JS_ES_6_FIELD_STATEMENT, CHILD_ELEM, TYPE_SCRIPT_FIELD};

    /**
     * fetches the resturn block of a given template function attribute
     * root element the class block
     */
    public static final Object[] TEMPLATE_RETURN_STMT = {TYPE_SCRIPT_FIELD, NAME_EQ("template"), JS_RETURN_STATEMENT};
    /**
     * fetches the string part of a given template attribute
     * root element the class block
     */
    public static final Object[] TEMPLATE_STR_LIT = {TYPE_SCRIPT_FIELD, NAME_EQ("template"), PSI_ELEMENT_JS_STRING_LITERAL};
    /**
     * fetches the identifier part opf a given template attribute
     * root element the class block
     */
    public static final Object[] TEMPLATE_IDENTIFIER = {TYPE_SCRIPT_FIELD, NAME_EQ("template"), PSI_ELEMENT_JS_IDENTIFIER};
    /**
     * feches the injecs for a given controller
     * root element the class block
     */
    public static final Object[] CONTROLLER_INJECTS = {ANY(TN_COMP_CONTROLLER_ARR, TN_COMP_CONTROLLER_FUNC_ARR), TN_COMP_STR_INJECTS};
    /**
     * fetches the parent function of a given element
     * root element anything whithin a function
     */
    public static final Object[] PARENT_FUNCTION = {PARENT_SEARCH(TYPE_SCRIPT_FUNC_EXPR, FIRST), CHILD_ELEM, JS_BLOCK_STATEMENT};
    /**
     * fetches the lock of a given function ({..} part)
     * root element anything which has functions
     */
    public static final Object[] FUNCTION_BLOCK = {TYPE_SCRIPT_FUNC_EXPR, JS_BLOCK_STATEMENT};
    /**
     * fetches the controller field
     * root element the class
     */
    public static final Object[] CONTROLLER_FIELD = {TYPE_SCRIPT_FIELD, NAME_EQ("controller")};
    public static final Object[] CONTROLLER_PROPERTY = {JS_PROPERTY, NAME_EQ("controller")};
    /**
     * potential candidates for the class attributes from the constructor function
     */
    public static final Object[] CLASS_VARIABLE_CANDIDATES = {CHILD_ELEM, JS_EXPRESSION_STATEMENT, TEXT_STARTS_WITH("this."), CHILD_ELEM, JS_ASSIGNMENT_EXPRESSION, CHILD_ELEM, JS_DEFINITION_EXPRESSION};

    protected Optional<PsiElementContext> lastImport;
    protected PsiElementContext rootBlock;
    protected PsiElementContext classBlock;
    protected String controllerAs;
    protected String bindToController;
    protected String restrict;
    protected List<Injector> injects; //imports into the constructor
    protected String selectorName; //trace back into the module declaration for this component and then run our string dash transformation
    protected String priority; //trace back into the module declaration for this component and then run our string dash transformation

    protected Optional<PsiElementContext> constructorDef;
    protected Optional<PsiElementContext> constructorBlock;
    protected Optional<PsiElementContext> transclude;

    protected List<FirstOrderFunction> inlineFunctions;
    protected List<ClassAttribute> possibleClassAttributes;
    protected String template; //original template after being found
    protected List<ComponentBinding> bindings;

    protected String clazzName;

    protected List<PsiElementContext> attributes;


    public List<String> getAttributesAsString() {
         return attributes.stream().map(attr -> attr.getName()).collect(Collectors.toList());
    }

    public AngularJSComponentTransformationModel(Project project, PsiFile psiFile, PsiElementContext rootBlock) {
        super(project, psiFile);
        applyRootBlock(psiFile, rootBlock);
        this.postConstruct2();
    }

    public void applyRootBlock(PsiFile psiFile, PsiElementContext rootBlock) {
        if (rootBlock != null) {
            this.rootBlock = rootBlock;
        } else {
            this.rootBlock = new PsiElementContext(psiFile);
        }
    }

    public AngularJSComponentTransformationModel(AnActionEvent event, PsiElementContext rootBlock) {
        super(event);
        applyRootBlock(getPsiFile(), rootBlock);
        this.postConstruct2();
    }

    public AngularJSComponentTransformationModel(Project project, VirtualFile virtualFile, PsiElementContext rootBlock) {
        super(project, virtualFile);
        applyRootBlock(getPsiFile(), rootBlock);
        this.postConstruct2();
    }

    public AngularJSComponentTransformationModel(IntellijFileContext fileContext) {
        super(fileContext);
        applyRootBlock(getPsiFile(), rootBlock);
        this.postConstruct2();
    }

    /**
     * we need as second postConstruct because
     * the block root must be present before running
     * into the post construct not sure how to
     * resolve this in a different manner, unless we introduce
     * IOC and get a dedicated post construct.
     */
    protected void postConstruct2() {

        /**
         * note splitting
         * of parsings,
         * but be carefule some of the parsings
         * rely on others, so
         * if you change the parsing element order
         * please check double you might break something
         */
        parseImport();
        parseClassBlock();

        parseConstructor();


        parseInjects();
        parseBindings();

        parseInlineFunctions();

        parseTemplate();
        parseClassName();
        parseSelectorName();
        parseControllerAs();
        parseBindToController();
        parseRestrict();
        parseTransclude();
        parseAttributes();
        parsePriority();
        parseInlineClassAttributeCandidates();
    }


    protected void parseClassName() {
        clazzName = classBlock.getName();

    }

    protected void parseTemplate() {
        Optional<PsiElementContext> returnStmt = classBlock.$q(TEMPLATE_RETURN_STMT).findFirst();
        Optional<PsiElementContext> stringTemplate = classBlock.$q(TEMPLATE_STR_LIT).findFirst();
        Optional<PsiElementContext> refTemplate = classBlock.$q(TEMPLATE_IDENTIFIER).findFirst();
        if (returnStmt.isPresent()) {
            PsiElementContext el = returnStmt.get();
            Optional<PsiElementContext> found = concat(el.$q(PSI_ELEMENT_JS_STRING_TEMPLATE_PART), concat(el.$q(PSI_ELEMENT_JS_STRING_LITERAL), el.$q(PSI_ELEMENT_JS_IDENTIFIER))).reduce((e1, e2) -> e2);
            if (found.isPresent() && found.get().getElement().toString().startsWith(PSI_ELEMENT_JS_IDENTIFIER)) {
                //identifier resolution
                template = found.get().getText();

            } else if (found.isPresent()) {
                //file resolution
                template = "`" + found.get().getUnquotedText() + "`";

            } else if (found.isPresent()) {
                //string resolution
                template = "``;//ERROR Template could not be resolved";
            }

        } else {
            if (stringTemplate.isPresent()) {
                PsiElementContext el = stringTemplate.get();
                template = "`" + el.getUnquotedText() + "`";
            } else if (refTemplate.isPresent()) {
                PsiElementContext el = refTemplate.get();
                template = "`" + el.getUnquotedText() + "`";
            } else {
                template = "``;//ERROR Template could not be resolved";
            }
        }


    }

    protected void parseInlineFunctions() {
        inlineFunctions = constructorBlock.get()
                //first we search for inline functions
                //including the variable definition aka let a = func...
                //or this.onInit = ....
                .$q(INLINE_FUNC_DECL)
                //now we have all function expressions
                //.filter(expr -> !Strings.isNullOrEmpty(expr.getIdentifier()))

                //then we drag the function data out to have a better grip on the variables etc...
                //if not parseable we will skip thje refactoring
                .map(this::parseFunctionData)
                .filter(Optional::isPresent)
                .filter(el -> {
                    return el.get().getFunctionElement().getParent().getTextRangeOffset() == constructorBlock.get().getTextRangeOffset();
                })
                .map(Optional::get)
                .collect(Collectors.toList());

    }

    /**
     * parses all variable declarations which happen directly under
     * a fixed parent function block
     *
     * @param parentFunctionBlock
     * @return
     */
    protected List<PsiElementContext> parseFunctionVariableDecls(PsiElementContext parentFunctionBlock) {

        return parentFunctionBlock.queryContent(JS_VAR_STATEMENT)
                .filter(e -> e.$q(PARENT_FUNCTION)
                        //only the first parent is valid, the variable must be declared
                        //in the parent function definition
                        .distinct()
                        //final check, the parent function found must be the parent block
                        .filter(e2 -> e2.getTextRangeOffset() == parentFunctionBlock.getTextRangeOffset()).findFirst().isPresent())
                .collect(Collectors.toList());


    }

    /**
     * trying to parse the details of the function data of the given
     * first order function
     *
     * @param inlineFunction the first order function block which should be determined
     * @return an optional details element, if the optional is absent
     * we can skip the externalization because some vital data might be missing
     * to produce again working code
     */
    protected Optional<FirstOrderFunction> parseFunctionData(PsiElementContext inlineFunction) {
        Optional<PsiElementContext> funtionDefinition = inlineFunction.$q(JS_DEFINITION_EXPRESSION).findFirst();
        Optional<PsiElementContext> functionBlock = inlineFunction.$q(JS_BLOCK_STATEMENT).findFirst();
        Optional<PsiElementContext> parameterList = inlineFunction.$q(TYPE_SCRIPT_PARAMETER_LIST).findFirst();

        //no constructor no externalization
        if (constructorBlock.isPresent() && funtionDefinition.isPresent() && functionBlock.isPresent()) {
            List<PsiElementContext> variableDecls = parseFunctionVariableDecls(this.getConstructorBlock().get());

            final Map<String, ExternalVariable> possibleInlineCandidates = new HashMap<>();

            variableDecls.stream().map(e -> new ExternalVariable(e)).forEach(e -> possibleInlineCandidates.put(e.getVariableName(), e));

            //now we check all calls into variables in the inline function
            //for the possibility of mapping into an external candidate

            List<ExternalVariable> foundExternalizables = inlineFunction.$q(JS_REFERENCE_EXPRESSION).map(el -> {
                String refExpr = el.getUnquotedText();
                String refName = refExpr.split("\\.")[0];
                return possibleInlineCandidates.containsKey(refName) ? possibleInlineCandidates.get(refName) : null;
            }).filter(e -> e != null).distinct().collect(Collectors.toList());

            List<ParameterDeclaration> parameters = parameterList.isPresent() ? parameterList.get().$q(TYPE_SCRIPT_PARAM).map(
                    param -> new ParameterDeclaration(param)
            ).collect(Collectors.toList()) : Collections.emptyList();


            return Optional.ofNullable(new FirstOrderFunction(inlineFunction, funtionDefinition.get(), functionBlock.get(), parameters, foundExternalizables));
        }
        return Optional.empty();
    }

    protected void parseInlineClassAttributeCandidates() {
        Set<String> ignore = new HashSet<>(getInjectsAsStr());
        ignore.addAll(getFunctionNames());
        possibleClassAttributes =
                constructorBlock.get().$q(JS_DEFINITION_EXPRESSION)
                        .filter(el -> isThis(el.getText()) && el.getText().split("\\.").length == 2)
                        .filter(isDirectConstructorCild())
                        .filter(notIgnorable(ignore))
                        .map(el -> new ClassAttribute(el))
                        .distinct()
                        .collect(Collectors.toList());


    }

    @NotNull
    protected List<String> getFunctionNames() {
        return inlineFunctions.stream().map(el -> {
            String functionName = el.getFunctionName();
            return functionName.substring(functionName.lastIndexOf(".") + 1);
        }).collect(Collectors.toList());
    }

    @NotNull
    protected List<String> getInjectsAsStr() {
        return injects.stream().map(el -> el.getName()).collect(Collectors.toList());
    }

    @NotNull
    protected Predicate<PsiElementContext> notIgnorable(Set<String> ignore) {
        return el -> {
            String name = el.getName();
            return !ignore.contains(name);
        };
    }

    @NotNull
    protected Predicate<PsiElementContext> isDirectConstructorCild() {
        return el -> {
            Optional<PsiElementContext> first = el.$q(PARENT_FUNCTION, FIRST).findFirst();
            return first.isPresent() && first.get().getTextOffset() == constructorBlock.get().getTextOffset();
        };
    }

    protected void parseImport() {
        lastImport =
                this.$q(ANY_TS_IMPORT).reduce((e1, e2) -> e2);
    }

    protected void parseClassBlock() {
        Stream<PsiElementContext> psiElementContextStream = rootBlock.$q(TYPE_SCRIPT_CLASS)
                .filter(el -> el.getText().contains("template") && el.getText().contains("controller"));
        classBlock = psiElementContextStream.findFirst().get();
    }

    protected void parseConstructor() {
        constructorDef = classBlock.$q(ALL(CONTROLLER_FIELD, CONTROLLER_PROPERTY)).findFirst();
        constructorBlock = constructorDef.get().$q(FUNCTION_BLOCK).findFirst();

    }

    protected void parseInjects() {
        injects = new ArrayList<>();
        List<String> strInjects =
                classBlock.$q(CONTROLLER_INJECTS)
                        .map(el -> el.getUnquotedText())
                        .collect(Collectors.toList());

        //static injects $inject must be there on IComponent level
        List<String> tsInjects = classBlock.$q(TN_COMP_PARAM_LISTS)
                .map(el -> el.getUnquotedText())
                .collect(Collectors.toList());

        for (int cnt = 0; cnt < tsInjects.size(); cnt++) {
            String tsNameType = tsInjects.get(cnt);
            String injector = (cnt < strInjects.size() - 1) ? strInjects.get(cnt) : tsNameType.split("\\:")[0];
            injects.add(new Injector(injector, tsNameType));
        }

    }

    protected void parseSelectorName() {

        Optional<String> selectorNameDefined =  classBlock.$q(TN_COMP_SELECTOR)
                .map(el -> el.getUnquotedText()).findFirst();
        if(selectorNameDefined.isPresent()) {
            selectorName = selectorNameDefined.get();
            return;
        }

        getSelectorFromDefinitionName();
    }

    protected void getSelectorFromDefinitionName() {
        selectorName = super.findFirstUpwards(el -> {
            Optional<PsiElementContext> ctx = new IntellijFileContext(getProject(), el).$q(TN_DEC_COMPONENT_NAME(clazzName)).findFirst();
            if (!ctx.isPresent()) {
                return false;
            }
            //TODO check imports
            return true;

        }).stream()
                .flatMap(el -> el.$q(TN_DEC_COMPONENT_NAME(clazzName)))
                .map(el2 -> el2.getText())
                .map(compName -> StringUtils.toDash(compName))
                .findFirst().orElse(StringUtils.toDash(getClazzName()));
    }

    protected void parseTransclude() {
        transclude = classBlock.$q(TN_COMP_TRANSCLUDE).findFirst();
    }

    protected void parseControllerAs() {
        controllerAs = classBlock.$q(TN_COMP_CONTROLLER_AS)
                .map(el -> el.getUnquotedText()).findFirst().orElse("ctrl");
    }

    protected void parsePriority() {
        priority = classBlock.$q(TN_COMP_PRIORITY)
                .map(el -> el.getUnquotedText()).findFirst().orElse(null);
    }



    protected void parseBindToController() {
        bindToController = classBlock.$q(TN_COMP_BIND_TO_CONTROLLER)
                .map(el -> el.getUnquotedText()).findFirst().orElse("true");
    }

    protected void parseRestrict() {
        restrict = classBlock.$q(TN_COMP_BIND_RESTRICT)
                .map(el -> el.getUnquotedText()).findFirst().orElse("E");
    }


    protected void parseAttributes() {
        attributes = classBlock.$q(CLASS_ATTRS)
                .filter(el -> isNotStandardAttr(el)
                )
                .collect(Collectors.toList());
    }

    protected boolean isNotStandardAttr(PsiElementContext el) {
        return !el.getName().equals("bindings") && !el.getName().equals("controllerAs")
                && !el.getName().equals("bindToController") && !el.getName().equals("restrict")
                && !el.getName().equals("controller") && !el.getName().equals("template")
                && !el.getName().equals("priority")&& !el.getName().equals("scope");
    }


    protected void parseBindings() {
        parseBindings(TN_COMP_BINDINGS);
    }

    protected void parseBindings(Object ... query) {
        bindings = classBlock.$q(query).map(el -> {
            String propName = el.getName();
            Optional<PsiElementContext> first = el.$q(PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
            String type = (first.isPresent()) ? first.get().getUnquotedText() : "<";
            return new ComponentBinding(BindingType.translate(type), propName);
        }).collect(Collectors.toList());
    }

    public String getInjectsStr() {
        return injects.stream().map(el -> el.toString()).reduce((str1, str2) -> str1 + ", " + str2).orElse("");
    }

    public String getRefactoredConstructorBlock() {


        List<IRefactorUnit> refactorings = inlineFunctions.stream()
                .filter(FirstOrderFunction::isExternalizale)
                .map(el -> {
                    PsiElementContext functionElement = el.getFunctionElement();
                    return new RefactorUnit(functionElement.getElement().getContainingFile(), functionElement, "");
                }).collect(Collectors.toList());

        String retVal = calculateRefactoring(refactorings, constructorBlock.get());


        //we transform the text into its own typescript shadow scratch file to perform
        //this refactorings against the injects

        refactoriThisIntoFunctions();

        return retVal;
    }

    public void refactoriThisIntoFunctions() {
        for (FirstOrderFunction inlineFunction : inlineFunctions) {
            if (!inlineFunction.isExternalizale()) {
                continue;
            }
            final TypescriptFileContext ctx = TypescriptFileContext.fromText(getProject(), inlineFunction.toExternalString());

            List<IRefactorUnit> injectionRefactorings = this.injects.stream()
                    //we look for all local variable references which match the injection
                    //TODO check why the name check fails
                    .flatMap(injector -> ctx.$q(matchInjection(injector)))
                    .distinct()
                    .map(foundRefExpr -> newThisRefactoring(ctx, foundRefExpr))
                    .collect(Collectors.toList());


            if (injectionRefactorings.isEmpty()) {
                continue;
            }
            inlineFunction.setRefactoredContent(ctx.calculateRefactoring(injectionRefactorings));
        }
    }

    @NotNull
    public RefactorUnit newThisRefactoring(TypescriptFileContext ctx, PsiElementContext foundRefExpr) {
        return new RefactorUnit(ctx.getPsiFile(), foundRefExpr, "this." + foundRefExpr.getText());
    }

    @NotNull
    protected Object[] matchInjection(Injector injector) {
        return new Object[]{PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ(injector.getName())};
    }

    @Nullable
    public String getTranscludeText() {
        if (transclude.isPresent()) {
            String text = transclude.get().getUnquotedText();
            return (text.contains("=")) ? text.substring(text.indexOf("=") + 1) : ((text.contains(":")) ? text.substring(text.indexOf(':') + 1) : text);
        }
        return null;
    }

    public String getImports() {
        return rootBlock.getText().substring(0, lastImport.get().getTextRangeOffset() + lastImport.get().getTextLength() + 1);
    }

    public String getFromImportsToClassDecl() {
        return rootBlock.getText().substring(lastImport.get().getTextRangeOffset() + lastImport.get().getTextLength() + 1, classBlock.getTextRangeOffset());
    }


    public String getCodeBetweenDefinitionAndClassBlock() {
        return "";
    }

}
