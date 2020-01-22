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

import com.google.common.base.Joiner;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jgoodies.common.base.Strings;
import lombok.Getter;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;
import net.werpu.tools.supportive.refactor.DummyInsertPsiElement;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.transformations.shared.ITransformationModel;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.*;
import net.werpu.tools.supportive.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.Ref;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.SUB_QUERY;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.*;
import static net.werpu.tools.supportive.transformations.tinydecs.AngularJSComponentTransformationModel.FUNCTION_BLOCK;
import static net.werpu.tools.supportive.transformations.tinydecs.AngularJSComponentTransformationModel.TEMPLATE_IDENTIFIER;

/**
 * Transformation model for the tiny decorations
 *
 * @See also AngularJSTransformationModel
 * <p>
 * <p>
 * resolution algorithm
 * watches will be replaced by empty strings and setters to the appropriate properties if possible
 * otherwise warning will be added that there is a deep search going on and it needs to be
 * resolved manually
 * <p>
 * injects will stay the same but a provide section will be added on top
 * input can stay as is
 * output can stay as is
 * AString needs to be remapped into its angular counterpart
 * <p>
 * also all props need to be added to a global index (different issue which then can be looked up
 * for prop types
 * <p>
 * all scope other references need to be replaced with this references
 * since a component is an isolated scope (wont be too many
 * outside of watch)
 * <p>
 * onInit etc.. need to be remapped into interface definitions
 * <p>
 * <p>
 * Template needs remapping (different topic, reliant on the component database)
 */
@Getter
public class TinyDecsComponentTransformationModel extends TypescriptFileContext implements ITransformationModel {
    public static final String ANN_INPUT = "Input";
    public static final String ANN_BOTH = "Both";
    public static final String ANN_FUNC = "Func";
    public static final String ANN_STRING = "AString";
    public static final String CONSTRUCTOR = "constructor";
    public static final String $_POST_LINK = "$postLink";
    public static final String $_ON_DESTROY = "$onDestroy";
    public static final String $_ON_CHANGES = "$onChanges";
    public static final String $_ON_INIT = "$onInit";
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
    protected Optional<PsiElementContext> annotationBlock;
    protected List<ClassAttribute> possibleClassAttributes;
    protected List<WatchBlockBinding> watches;
    protected Optional<PsiElementContext> postLinkDef;
    protected Optional<PsiElementContext> onInitDef;
    protected Optional<PsiElementContext> onChangesDef;
    protected List<DestroyBinding> destroyDef;
    protected String template; //original template after being found
    protected List<ComponentBinding> bindings;
    protected String clazzName;
    protected List<PsiElementContext> attributes;
    protected List<PsiElementContext> passThroughMethods;

    public TinyDecsComponentTransformationModel(Project project, PsiFile psiFile) {
        super(project, psiFile);
        this.postConstruct2();
    }

    public TinyDecsComponentTransformationModel(AnActionEvent event) {
        super(event);
        this.postConstruct2();
    }

    public TinyDecsComponentTransformationModel(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
        this.postConstruct2();
    }

    public TinyDecsComponentTransformationModel(IntellijFileContext fileContext) {
        super(fileContext);
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
        parseComponentAnnotation();
        parseSelectorName();
        parseControllerAs();
        parseTemplate();

        parseBindings();
        parseInjects();
        parseAttributes();
        parseWatches();

        parsePostLinkDef();
        parseOnInitDef();
        parseClassBlock();
        parseOnChanges();
        parseConstructor();
        parseOnDestroy();

        parsePassThroughMethods();

        /*
        parseConstructor();
        parseInlineFunctions();
        parseTemplate();
        parseRestrict();
        parseTransclude();
        parseAttributes();
        parsePriority();
        parseInlineClassAttributeCandidates();
        */
    }

    /**
     * parse the import positions
     */
    protected void parseImport() {
        lastImport =
                this.$q(ANY_TS_IMPORT).reduce((e1, e2) -> e2);
    }

    /**
     * parse the beginning of the class block
     */
    protected void parseClassBlock() {
        this.rootBlock = new PsiElementContext(this.getPsiFile());
        Stream<PsiElementContext> psiElementContextStream = rootBlock.$q(TYPE_SCRIPT_CLASS)
                .filter(el -> el.getText().contains("template") && el.getText().contains("controller"));
        classBlock = psiElementContextStream.findFirst().get();
        clazzName = classBlock.getName();
    }

    /**
     * parse the component annotation
     *
     * @return
     */
    private boolean parseComponentAnnotation() {
        Optional<PsiElementContext> decorator = rootBlock.$q(COMPONENT_ANN).findFirst();
        if (!decorator.isPresent()) {
            return true;
        }
        this.annotationBlock = decorator;
        return false;
    }

    /**
     * parses the selector data
     */
    void parseSelectorName() {

        Optional<PsiElementContext> selectorContext = annotationBlock.get().$q(JS_PROPERTY, NAME_EQ("selector"), JS_LITERAL_EXPRESSION, PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
        if (!selectorContext.isPresent()) {
            return;
        }
        selectorName = selectorContext.get().getUnquotedText();
    }

    /**
     * parses the controller as data
     */
    void parseControllerAs() {
        Optional<PsiElementContext> controllerAsContext = annotationBlock.get().$q(JS_PROPERTY, PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("controllerAs"), PARENT, JS_LITERAL_EXPRESSION, PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
        if (!controllerAsContext.isPresent()) {
            this.controllerAs = "ctrl";
            return;
        }
        this.controllerAs = StringUtils.stripQuotes(controllerAsContext.get().getText());
    }

    /**
     * parse the template and resolves the template text
     */
    protected void parseTemplate() {
        Optional<PsiElementContext> returnStmt = annotationBlock.get().$q(ANNOTATED_TEMPLATE(JS_RETURN_STATEMENT)).findFirst();
        Optional<PsiElementContext> stringTemplate = annotationBlock.get().$q(ANNOTATED_TEMPLATE(JS_STRING_TEMPLATE_EXPRESSION)).findFirst();
        Optional<PsiElementContext> stringRef = annotationBlock.get().$q(ANNOTATED_TEMPLATE(PSI_ELEMENT_JS_STRING_LITERAL)).findFirst();
        Optional<PsiElementContext> refTemplate = annotationBlock.get().$q(TEMPLATE_IDENTIFIER).findFirst();

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
                //TODO resolve external ref
                PsiElementContext el = refTemplate.get();
                template = "`" + el.getUnquotedText() + "`";
            } else {
                template = "``;//ERROR Template could not be resolved";
            }
        }

    }

    public String getFromImportsToClassDecl() {
        return rootBlock.getText().substring(lastImport.get().getTextRangeOffset() + lastImport.get().getTextLength() + 1, classBlock.getTextRangeOffset());
    }

    public String getImports() {
        String imports = rootBlock.getText().substring(0, lastImport.get().getTextRangeOffset() + lastImport.get().getTextLength() + 1);
        String[] importsPerLine = imports.split("\\n+");
        return Arrays.stream(importsPerLine)
                .filter(line -> {
                    return !line.contains("TinyDecorations");
                })
                .reduce("", (target, line) -> target + "\n" + line);
    }

    //------------------- helpers -------------------------------------------

    /**
     * parse the component bindings for remapping
     */
    private void parseBindings() {
        this.bindings = classBlock.$q(JS_ES_6_FIELD_STATEMENT).filter(ann -> {
            Optional<PsiElementContext> ident = ann.$q(JS_ES_6_DECORATOR, PSI_ELEMENT_JS_IDENTIFIER).reduce((el1, el2) -> el2);
            if (!ident.isPresent()) {
                return false;
            }
            String text = ident.get().getText();
            return isAnnotatedBinding(text);
        }).map(ann -> {
            PsiElementContext ident = ann.$q(JS_ES_6_DECORATOR, PSI_ELEMENT_JS_IDENTIFIER).reduce((el1, el2) -> el2).get();
            boolean optional = ann.$q(JS_LITERAL_EXPRESSION, PSI_ELEMENT_JS_TRUE_KEYWORD).findFirst().isPresent();
            PsiElementContext annRoot = ann.$q(PARENTS_EQ_FIRST(JS_ES_6_FIELD_STATEMENT)).findFirst().get();
            PsiElementContext identifier = annRoot.$q(PSI_ELEMENT_JS_IDENTIFIER).reduce((el1, el2) -> el2).get();
            Optional<PsiElementContext> type = annRoot.$q(TYPE_SCRIPT_SINGLE_TYPE).reduce((el1, el2) -> el2);

            BindingType bindingType = null;
            String identStr = ident.getText().toUpperCase();
            if (optional) {
                bindingType = BindingType.valueOf("OPT_" + identStr);
            } else {
                bindingType = BindingType.valueOf(identStr);
            }

            return new ComponentBinding(bindingType, identifier.getUnquotedText(), type.isPresent() ? type.get().getText() : "any");
        }).collect(Collectors.toList());

    }

    /**
     * parse the injects for further processing
     */
    private void parseInjects() {
        this.injects = rootBlock.$q(TYPE_SCRIPT_FUNC, NAME_EQ(CONSTRUCTOR), DIRECT_CHILD(TYPE_SCRIPT_PARAMETER_LIST), TYPE_SCRIPT_PARAM)
                .map(el -> {
                    String name = el.getName();
                    Optional<PsiElementContext> type = el.$q(TYPE_SCRIPT_SINGLE_TYPE).reduce((el1, el2) -> el2);
                    Optional<PsiElementContext> ref = el.$q(ANY(SUB_QUERY(JS_ES_6_DECORATOR, JS_ARGUMENTS_LIST, PSI_ELEMENT_JS_STRING_LITERAL),
                            SUB_QUERY(JS_ES_6_DECORATOR, JS_ARGUMENTS_LIST, JS_REFERENCE_EXPRESSION))).findFirst();
                    return new Injector(ref.get().getText(), name, type.isPresent() ? type.get().getText() : "any");
                }).collect(Collectors.toList());

    }

    /**
     * parse the class attributes/properties
     */
    private void parseAttributes() {
        possibleClassAttributes = rootBlock.$q(TYPE_SCRIPT_CLASS, DIRECT_CHILD(JS_ES_6_FIELD_STATEMENT))
                .filter(el -> !el.$q(JS_ES_6_DECORATOR).findFirst().isPresent())
                .map(el -> {
                    String name = el.$q(TYPE_SCRIPT_FIELD).findFirst().get().getName();
                    String typeName = resolveType(el);
                    return new ClassAttribute(el, name, typeName);
                }).collect(Collectors.toList());
    }

    private String resolveType(PsiElementContext el) {
        Optional<PsiElementContext> type = el.$q(ANY(TYPE_SCRIPT_ARRAY_TYPE, TYPE_SCRIPT_SINGLE_TYPE)).findFirst();

        return type.isPresent() ? type.get().getText() : "any";
    }

    //watches need to be transformed into setters

    /**
     * parse the watch blocks in the code and parse its meta data
     * for further transformations
     */
    private void parseWatches() {
        this.watches = rootBlock.$q(JS_CALL_EXPRESSION).filter(el -> {
            Optional<PsiElementContext> ident = el.$q(PSI_ELEMENT_JS_IDENTIFIER).filter(el2 -> {
                return el2.getText().equals("$watch");
            }).findFirst();

            return ident.isPresent();
        }).map(el -> {
            Optional<PsiElementContext> funcStart = el.$q(TYPESCRIPT_FUNCTION_EXPRESSION).findFirst();
            if (!funcStart.isPresent()) {
                return null;
            }

            //resolve the parameters
            List<PsiElementContext> params = funcStart.get().$q(DIRECT_CHILD(TYPE_SCRIPT_PARAMETER_LIST), TYPE_SCRIPT_PARAM).collect(Collectors.toList());
            String paramName1 = null;
            String paramType1 = null;
            String paramName2 = null;
            String paramType2 = null;

            String propName = el.$q(JS_ARGUMENTS_LIST, PSI_ELEMENT_JS_STRING_LITERAL).findFirst().orElseGet(
                    () -> el.$q(JS_ARGUMENTS_LIST, PSI_ELEMENT_JS_STRING_TEMPLATE_PART).findFirst().get()
            ).getUnquotedText();

            if (propName.startsWith(this.controllerAs + ".")) {
                propName = propName.substring(this.controllerAs.length() + 1);
            }

            if (params.size() > 0) {
                paramName1 = params.get(0).getName();
                paramType1 = resolveType(params.get(0));
            }

            if (params.size() > 1) {
                paramName2 = params.get(1).getName();
                paramType2 = resolveType(params.get(1));
            }

            return new WatchBlockBinding(funcStart.get(), propName, paramName1, paramType1, paramName2, paramType2);

        })
                .filter(binding -> binding != null)
                .collect(Collectors.toList());
    }

    private void parsePostLinkDef() {
        this.postLinkDef = classBlock.$q(TYPE_SCRIPT_FUNC, NAME_EQ($_POST_LINK)).findFirst();
    }

    private void parseOnInitDef() {
        this.onInitDef = classBlock.$q(TYPE_SCRIPT_FUNC, NAME_EQ("$onInit")).findFirst();
    }

    private void parseOnChanges() {
        this.onChangesDef = classBlock.$q(TYPE_SCRIPT_FUNC, NAME_EQ($_ON_CHANGES)).findFirst();
    }

    private void parseOnDestroy() {
        this.destroyDef = new ArrayList<>();
        Optional<PsiElementContext> destroyFunction = classBlock.$q(TYPE_SCRIPT_FUNC, NAME_EQ($_ON_DESTROY)).findFirst();
        if (destroyFunction.isPresent()) {
            destroyDef.add(new DestroyBinding(destroyFunction.get(), null));
        }

        List<PsiElementContext> destroyWatches = classBlock.$q(JS_EXPRESSION_STATEMENT, JS_REFERENCE_EXPRESSION, ALL(TEXT_CONTAINS("$on"), TEXT_CONTAINS("$destroy")), PARENT, JS_EXPRESSION_STATEMENT)
                .collect(Collectors.toList());

        List<DestroyBinding> rets = destroyWatches.stream().map((final PsiElementContext destroyDef) -> {
            Optional<PsiElementContext> functionExpr = destroyDef.$q(DIRECT_CHILD(TYPESCRIPT_FUNCTION_EXPRESSION)).reduce((el1, el2) -> el2);
            Optional<PsiElementContext> referenceExpr = destroyDef.$q(DIRECT_CHILD(JS_REFERENCE_EXPRESSION)).reduce((el1, el2) -> el2);

            //either or or nothing
            if (functionExpr.isPresent()) {
                return new DestroyBinding(destroyDef, functionExpr.get());
            } else if (referenceExpr.isPresent()) {
                Optional<PsiElementContext> functionDeclaration = resolveDeclaration(referenceExpr);
                if (functionDeclaration.isPresent()) {
                    return new DestroyBinding(destroyDef, functionDeclaration.get());
                }
            }
            return null;

        }).filter(el -> el != null)
                .collect(Collectors.toList());
        this.destroyDef.addAll(rets);
    }

    /**
     * resolves the declaration of a given function
     *
     * @param referenceExpr
     * @return
     */
    @NotNull
    private Optional<PsiElementContext> resolveDeclaration(Optional<PsiElementContext> referenceExpr) {
        String name = referenceExpr.get().getName();
        return referenceExpr.get().$q(PARENTS, JS_BLOCK_STATEMENT, JS_VAR_STATEMENT, DIRECT_CHILD(TYPE_SCRIPT_VARIABLE), NAME_EQ(name)).findFirst();
    }

    protected void parseConstructor() {
        constructorDef = classBlock.$q(TYPE_SCRIPT_FUNC, NAME_EQ(CONSTRUCTOR)).findFirst();
        constructorBlock = constructorDef.get().$q(FUNCTION_BLOCK).findFirst();
    }

    protected void parsePassThroughMethods() {
        this.passThroughMethods = classBlock.$q(DIRECT_CHILD(TYPE_SCRIPT_FUNC)).filter(func -> {
            String name = func.getName();
            return !Strings.isBlank(name) && !isLifecycleHook(name);
        }).collect(Collectors.toList());

    }

    private boolean isLifecycleHook(String name) {
        return name.equals(CONSTRUCTOR) ||
                name.equals($_POST_LINK) ||
                name.equals($_ON_INIT) ||
                name.equals($_ON_DESTROY) ||
                name.equals($_ON_CHANGES);
    }

    private boolean isAnnotatedBinding(String text) {
        return text.equals(ANN_INPUT) ||
                text.equals(ANN_BOTH) ||
                text.equals(ANN_FUNC) ||
                text.equals(ANN_STRING);
    }

    //refatorMethods
    //we need to apply following refactorings
    //
    //TODO we probably have to refactor in multiple passes
    //to refactor nested definitions out, for the time being lets
    //only do it within one pass and do not go any deeper
    //we need one pass per call depth to get everything right
    //after ever pass we need another parsing of the refactored file
    private String refactorMethodBlock(PsiElementContext methodBody) {

        List<RefactorUnit> refactorings = new LinkedList<>();
        refactorings.addAll(refactorFuntionBindingCalls(methodBody));

        // this.bindingname.emit(value...)

        //$scope.$watch("<crlAs>.<attribute>") ->
        //binding -> onChanges entry...
        //no binding -> setter with shadow old value, getter with new value
        // set <attribute>(newValue: type) {
        //    oldValue = this._<attrbute>;
        //    <existing code>
        //   this._<attribute> = newValue;
        //}

        //in case of deep watches perform a deep compare leave the watch alone and set a warning that this code could not be transformated
        //TODO routine has to be written/copied yet from lodash
        //

        //$scope.$on for the time being place a warning in front, unless destroy, postlink etc.. move this code into the appropriate locations
        //TODO in the long run replace on message handlers with the proposed reactive solutions

        //simple $scope variable references, add attributes and replace them with this and public attributes unless they do not already exist

        //$compile reference, place a warning.. TODO check what angular proposes for $compile replacement

        //TODO add other refactorings on the fly on a need to know base

        return "TODO";
    }

    //TODO we probably have to move this into resolvers... because this is stuff we
    //will have to reuse in other parts of the system

    //function binding...
    //call
    //this.<bindingname>({
    // param: value
    // }} needs to be replaced by this.<bindingName>(value, value2 etc...)
    List<RefactorUnit> refactorFuntionBindingCalls(PsiElementContext methodBlock) {

        final List<RefactorUnit> ret = new LinkedList<>();

        final Set<String> functionBindings = getFunctionBindingNames();

        List<PsiElementContext> methodCalls = methodBlock.$q(JS_EXPRESSION_STATEMENT, JS_CALL_EXPRESSION, JS_REFERENCE_EXPRESSION, DIRECT_CHILD(PSI_ELEMENT_JS_IDENTIFIER))
                .filter(ctx -> functionBindings.contains(ctx.getUnquotedText()))
                .map(ctx -> ctx.$q(JS_CALL_EXPRESSION).findFirst().get())
                .collect(Collectors.toList());

        //now we have all method calls
        methodCalls.stream().forEach(methodCall -> {

            String callBody = methodCall.getText().substring(0, methodCall.getText().indexOf("("));
            List<String> methodParams = methodCalls.stream()
                    .map(ctx -> ctx.$q(JS_CALL_EXPRESSION).findFirst().get())
                    .flatMap(ctx -> ctx.$q(DIRECT_CHILD(JS_ARGUMENTS_LIST), DIRECT_CHILD(JS_OBJECT_LITERAL_EXPRESSION), CHILD_ELEM, ANY(JS_LITERAL_EXPRESSION, TYPE_SCRIPT_FUNC_EXPR, JS_STRING_TEMPLATE_EXPRESSION)))
                    .map(ctx -> ctx.getText())
                    .collect(Collectors.toList());

            if (methodParams.isEmpty()) {
                return;
            }
            RefactorUnit refactorUnit = new RefactorUnit(methodCall.getElement().getContainingFile(), methodCall, callBody + "(" + Joiner.on(", ").join(methodParams) + ")");

            ret.add(refactorUnit);
        });

        return ret;
    }

    /**
     * returns a filtered set of function binding names
     *
     * @return
     */
    @NotNull
    private Set<String> getFunctionBindingNames() {
        return this.bindings.stream().filter(binding -> binding.getBindingType() == BindingType.FUNC || binding.getBindingType() == BindingType.OPT_FUNC)
                .map(binding -> binding.getName())
                .collect(Collectors.toSet());
    }

    /**
     * @param methodBlock         method block containing the scope definitions
     * @param onChangesTarget     refactor inserts for the changes block, which have an unclear position as of yet
     * @param setterGetterTargets refactor inserts for the setter and getter blocks, which have an unclear position os of yet
     * @return a set of generic clears which should clear out the code parts which are refactored without any replacement
     */
    private List<RefactorUnit> refactorWatches(PsiElementContext methodBlock, List<RefactorUnit> onChangesTarget, List<RefactorUnit> setterGetterTargets) {
        //$scope.$watch("<crlAs>.<attribute>") ->
        //binding -> onChanges entry...
        //no binding -> setter with shadow old value, getter with new value
        // set <attribute>(newValue: type) {
        //    oldValue = this._<attrbute>;
        //    <existing code>
        //   this._<attribute> = newValue;
        //}

        final List<RefactorUnit> inCodeRefactorings = new LinkedList<>();
        List<PsiElementContext> watches = methodBlock.$q(JS_EXPRESSION_STATEMENT, DIRECT_CHILD(JS_CALL_EXPRESSION), DIRECT_CHILD(JS_REFERENCE_EXPRESSION), TEXT_CONTAINS("$scope.$watch"), PARENTS_EQ_FIRST(JS_CALL_EXPRESSION)).collect(Collectors.toList());
        watches.stream().forEach(element -> {
            Optional<PsiElementContext> watched = element.$q(DIRECT_CHILD(JS_ARGUMENTS_LIST), DIRECT_CHILD(PSI_ELEMENT_JS_STRING_LITERAL)).findFirst();
            if (!watched.isPresent()) {
                return;
            }
            final Set<String> bindingNames = getBindingNames();
            String[] watchedStr = watched.get().getUnquotedText().split(".");
            Optional<String> watchedAttr = Arrays.stream(watchedStr).filter(name -> bindingNames.contains(name)).findFirst();
            if (watchedAttr.isPresent()) {
                //new onchanges entry
                handleOnChangesRefactoring(element, inCodeRefactorings, onChangesTarget);
            } else {
                handleSetterGetterRefactoring(element, inCodeRefactorings, setterGetterTargets, watchedAttr);
            }
        });
        return inCodeRefactorings;
    }

    private void handleSetterGetterRefactoring(PsiElementContext element, List<RefactorUnit> inCodeRefactorings, List<RefactorUnit> setterGetterTargets, Optional<String> watchedAttr) {
        Optional<PsiElementContext> callbackFunction = element.$q(DIRECT_CHILD(JS_ARGUMENTS_LIST), DIRECT_CHILD(TYPE_SCRIPT_FUNC_EXPR)).findFirst();
        if (!callbackFunction.isPresent()) {
            RefactorUnit noRefactoringWarning = createNoRefactoringWarning(element);
            inCodeRefactorings.add(noRefactoringWarning);
        } else {
            PsiElementContext callback = callbackFunction.get();

            //new setter and getter and protected _<attrName> variable;
            String newValueName = callback.$q(TYPE_SCRIPT_PARAM).map(el -> el.getName()).findFirst().orElse("newValue");
            String oldValueName = callback.$q(TYPE_SCRIPT_PARAM).map(el -> el.getName()).reduce("oldValue", (name1, name2) -> name2);
            if (newValueName.equals(oldValueName)) {//only one param which means automatically newValue
                oldValueName = "oldValue";
            }
            String attrName = watchedAttr.get();
            String varName = "_"+attrName;
            String attrType = callback.$q(TYPE_SCRIPT_PARAM, ANY(TYPE_SCRIPT_ARRAY_TYPE, TYPE_SCRIPT_SINGLE_TYPE)).map(theType -> theType.getText()).findFirst().orElse("any");
            String codeBlock = callback.$q(JS_BLOCK_STATEMENT).map(el -> el.getText()).findFirst().orElse("{}");
            codeBlock = codeBlock.substring(1, codeBlock.length() - 1);


            //0 attrName, 1 varName, 2 attrType, 3 newValueName, 4 oldValueName, 5 codeBlock
            StringBuilder refTxt = new StringBuilder();
            refTxt.append("protected {1}: {2}};\n\n");
            refTxt.append("get {0} {\n");
            refTxt.append("     return this.{1};\n");
            refTxt.append("}\n\n");

            refTxt.append("set {0}({3}: {2}) {\n");
            refTxt.append("   let {4} = {1} \n ");
            refTxt.append("   this.{1} = {3}; \n ");
            refTxt.append("   {5}\n");
            refTxt.append("}\n");


            String finalRefTxt = String.format(refTxt.toString(), new Object[]{
                attrName, varName, attrType, newValueName, oldValueName, codeBlock
            });

            RefactorUnit setterGetterAttrInsert = new RefactorUnit(element.getElement().getContainingFile(), new DummyInsertPsiElement(0), finalRefTxt);
            setterGetterTargets.add(setterGetterAttrInsert);
        }
    }

    /**
     * handles the onchanges refactoring part which is
     * move the code if possible to the onchanges function
     * if not but we have a classical attribute watch pattern
     * place an in code warning for manual interference
     */
    private void handleOnChangesRefactoring(PsiElementContext element, List<RefactorUnit> inCodeWarnings, List<RefactorUnit> onChangesTarget) {
        Optional<PsiElementContext> callbackFunction = element.$q(DIRECT_CHILD(JS_ARGUMENTS_LIST), DIRECT_CHILD(TYPE_SCRIPT_FUNC_EXPR)).findFirst();
        if (!callbackFunction.isPresent()) {
            RefactorUnit noRefactoringWarning = createNoRefactoringWarning(element);
            inCodeWarnings.add(noRefactoringWarning);
        } else {
            PsiElementContext callback = callbackFunction.get();
            //we now have the structure of a callback first order function
            //lets determine the old and newValue, oldValue being optional, newValue as well
            String newValueName = callback.$q(TYPE_SCRIPT_PARAM).map(el -> el.getName()).findFirst().orElse("newValue");
            String oldValueName = callback.$q(TYPE_SCRIPT_PARAM).map(el -> el.getName()).reduce("oldValue", (name1, name2) -> name2);
            if (newValueName.equals(oldValueName)) {//only one param which means automatically newValue
                oldValueName = "oldValue";
            }

            String codeBlock = callback.$q(JS_BLOCK_STATEMENT).map(el -> el.getText()).findFirst().orElse("{}");
            codeBlock = codeBlock.substring(1, codeBlock.length() - 1);
            StringBuilder refTxt = new StringBuilder();
            refTxt.append("if(changes?.{0}) {\n");
            refTxt.append("    let {1} = changes?.{0}?.newValue; \n");
            refTxt.append("    let {2} = changes?.{0}?.oldValue; \n");
            refTxt.append("         {3}");
            refTxt.append("}");

            String finalRefTxt = String.format(refTxt.toString(), new Object[]{oldValueName, newValueName, codeBlock});

            RefactorUnit onChangesInsert = new RefactorUnit(element.getElement().getContainingFile(), new DummyInsertPsiElement(0), finalRefTxt);
            onChangesTarget.add(onChangesInsert);
        }
    }

    @NotNull
    private RefactorUnit createNoRefactoringWarning(PsiElementContext element) {
        StringBuilder finalRefTxt = new StringBuilder();
        finalRefTxt.append("//warning this code part could not be safely refactored, you have to do it yourself \n");
        finalRefTxt.append(element.getText());
        return new RefactorUnit(element.getElement().getContainingFile(), element, finalRefTxt.toString());
    }

    @NotNull
    private Set<String> getBindingNames() {
        return this.bindings.stream().map(binding -> binding.getName()).collect(Collectors.toSet());
    }

    List<RefactorUnit> refactorOns(PsiElementContext methodBlock) {
        return Collections.emptyList();
    }

    List<RefactorUnit> refactorRemainingScopes(PsiElementContext methodBlock) {
        return Collections.emptyList();
    }

    List<RefactorUnit> refactorCompiles(PsiElementContext methodBlock) {
        return Collections.emptyList();
    }

}