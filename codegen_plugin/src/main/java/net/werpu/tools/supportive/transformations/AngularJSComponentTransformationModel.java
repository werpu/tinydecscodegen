package net.werpu.tools.supportive.transformations;

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
import net.werpu.tools.supportive.transformations.modelHelpers.*;
import net.werpu.tools.supportive.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.*;


@Getter
public class AngularJSComponentTransformationModel extends TypescriptFileContext implements ITransformationModel {


    public static final Object[] INLINE_FUNC_DECL = {TYPE_SCRIPT_FUNC_EXPR, TreeQueryEngine.PARENTS_EQ_FIRST(JS_EXPRESSION_STATEMENT)};
    Optional<PsiElementContext> lastImport;
    PsiElementContext rootBlock;
    PsiElementContext classBlock;
    String controllerAs;
    List<Injector> injects; //imports into the constructor
    String selectorName; //trace back into the module declaration for this component and then run our string dash transformation

    Optional<PsiElementContext> constructorDef;
    Optional<PsiElementContext> constructorBlock;
    Optional<PsiElementContext> transclude;

    List<FirstOrderFunction> inlineFunctions;
    String template; //original template after being found
    List<BindingTypes> bindings;

    String clazzName;

    List<PsiElementContext> attributes;







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

    protected void postConstruct2() {

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
        parseTransclude();
        parseAttributes();

    }

    private void parseClassName() {
        Optional<PsiElementContext> clazzDcl = rootBlock.$q(TYPE_SCRIPT_CLASS).findFirst();
        if (clazzDcl.isPresent()) {
            clazzName = clazzDcl.get().getName();
        }
        //TODO look it up in the parent module
    }

    private void parseTemplate() {
        Optional<PsiElementContext> returnStmt = rootBlock.$q(TYPE_SCRIPT_FIELD, TreeQueryEngine.NAME_EQ("template"), JS_RETURN_STATEMENT).findFirst();
        returnStmt.ifPresent((el) -> {
            Optional<PsiElementContext> found = Stream.concat(el.$q(PSI_ELEMENT_JS_STRING_LITERAL), el.$q(PSI_ELEMENT_JS_IDENTIFIER)).findFirst();
            if (found.isPresent() && found.get().getElement().toString().startsWith(PSI_ELEMENT_JS_IDENTIFIER)) {
                //identifier resolution
            } else if (found.isPresent() && found.get().getElement().getText().endsWith(".html")) {
                //file resolution
            } else if (found.isPresent()) {
                //string resolution
            }
        });

    }

    private void parseInlineFunctions() {
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
    private List<PsiElementContext> parseFunctionVariableDecls(PsiElementContext parentFunctionBlock) {

        return parentFunctionBlock.queryContent(JS_VAR_STATEMENT)
                .filter(e -> e.$q(PARENT_SEARCH(TYPE_SCRIPT_FUNC_EXPR, FIRST), CHILD_ELEM, JS_BLOCK_STATEMENT)
                        //only the first parent is valid, the variable must be declared
                        //in the parent function definition
                        .distinct()
                        //final check, the parent function found must be the parent block
                        .filter(e2 -> e2.getTextOffset() == parentFunctionBlock.getTextOffset()).findFirst().isPresent())
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
    private Optional<FirstOrderFunction> parseFunctionData(PsiElementContext inlineFunction) {
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
                String refExpr = el.getText();
                String refName = refExpr.split("\\.")[0];
                return possibleInlineCandidates.containsKey(refName) ? possibleInlineCandidates.get(refName) : null;
            }).filter(e -> e != null).distinct().collect(Collectors.toList());

            List<ParameterDeclaration> parameters = parameterList.isPresent() ?  parameterList.get().$q(TYPE_SCRIPT_PARAM).map(
                    param -> new ParameterDeclaration(param)
            ).collect(Collectors.toList()) : Collections.emptyList();


            return Optional.ofNullable(new FirstOrderFunction(inlineFunction, funtionDefinition.get(), functionBlock.get(), parameters, foundExternalizables));
        }
        return Optional.empty();
    }

    private void parseImport() {
        lastImport = this.$q(JS_ES_6_IMPORT_DECLARATION).reduce((e1, e2) -> e2);
    }

    private void parseClassBlock() {
        classBlock = rootBlock.$q(TYPE_SCRIPT_CLASS).findFirst().get();
    }

    private void parseConstructor() {
        constructorDef = rootBlock.$q(TYPE_SCRIPT_FIELD, TreeQueryEngine.NAME_EQ("controller")).findFirst();
        constructorBlock = constructorDef.get().$q(TYPE_SCRIPT_FUNC_EXPR, JS_BLOCK_STATEMENT).findFirst();

    }

    private void parseInjects() {
        injects = new ArrayList<>();
        List<String> strInjects =
                rootBlock.$q(TN_COMP_CONTROLLER_ARR, TN_COMP_STR_INJECTS)
                        .map(el -> el.getText())
                        .collect(Collectors.toList());

        List<String> tsInjects = rootBlock.$q(TN_COMP_PARAM_LISTS)
                .map(el -> el.getText())
                .collect(Collectors.toList());

        for (int cnt = 0; cnt < tsInjects.size(); cnt++) {
            String tsNameType = tsInjects.get(cnt);
            String injector = (cnt < strInjects.size() - 1) ? strInjects.get(cnt) : tsNameType.split("\\:")[0];
            injects.add(new Injector(injector, tsNameType));
        }

    }

    private void parseSelectorName() {
        String className = this.$q(TYPE_SCRIPT_CLASS).findFirst().map(el -> el.getName()).orElse("????");

        selectorName = super.findFirstUpwards(el -> {
            Optional<PsiElementContext> ctx = new IntellijFileContext(getProject(), el).$q(TN_DEC_COMPONENT_NAME(className)).findFirst();
            if (!ctx.isPresent()) {
                return false;
            }
            //TODO check imports
            return true;

        }).stream()
                .flatMap(el -> el.$q(TN_DEC_COMPONENT_NAME(className)))
                .map(el2 -> el2.getText())
                .map(compName -> StringUtils.toDash(compName))
                .findFirst().orElse(StringUtils.toDash(getClazzName()));
    }

    private void parseTransclude() {
        transclude = rootBlock.$q(TN_COMP_TRANSCLUDE).findFirst();
    }

    private void parseControllerAs() {
        controllerAs = classBlock.$q(TN_COMP_CONTROLLER_AS)
                .map(el -> el.getText()).findFirst().orElse("ctrl");
    }

    private void parseAttributes() {
        attributes = classBlock.$q(CHILD_ELEM, JS_ES_6_FIELD_STATEMENT, CHILD_ELEM, TYPE_SCRIPT_FIELD)
                .filter(el -> !el.getName().equals("bindings") && !el.getName().equals("controllerAs")
                        && !el.getName().equals("controller") && !el.getName().equals("template")
                        && !el.getName().equals("transclude")
                )
                .collect(Collectors.toList());
    }


    private void parseBindings() {
        bindings = classBlock.$q(TN_COMP_BINDINGS).map(el -> {
            String propName = el.getName();
            Optional<PsiElementContext> first = el.$q(PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
            String type = (first.isPresent()) ? first.get().getText() : "<";
            return new BindingTypes(BindingType.translate(type), propName);
        }).collect(Collectors.toList());
    }

    public String getInjectsStr() {
        return injects.stream().map(el -> el.toString()).reduce((str1, str2) -> str1 + "," + str2).orElse("");
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

        for (FirstOrderFunction inlineFunction : inlineFunctions) {
            if (!inlineFunction.isExternalizale()) {
                continue;
            }
            final TypescriptFileContext ctx = TypescriptFileContext.fromText(getProject(), inlineFunction.toExternalString());

            List<IRefactorUnit> injectionRefactorings = this.injects.stream()
                    //we look for all local variable references which match the injection
                    //TODO check why the name check fails
                    .flatMap(injector ->  ctx.$q(matchInjection(injector)))
                    .distinct()
                    .map(foundRefExpr -> newThisRefactoring(ctx, foundRefExpr))
                    .collect(Collectors.toList());

            if(injectionRefactorings.isEmpty()) {
                continue;
            }
            inlineFunction.setRefactoredContent(ctx.calculateRefactoring(injectionRefactorings));
        }

        return retVal;
    }

    @NotNull
    public RefactorUnit newThisRefactoring(TypescriptFileContext ctx, PsiElementContext foundRefExpr) {
        return new RefactorUnit(ctx.getPsiFile(), foundRefExpr, "this." + foundRefExpr.getText());
    }

    @NotNull
    private Object[] matchInjection(Injector injector) {
        return new Object[]{PSI_ELEMENT_JS_IDENTIFIER, TreeQueryEngine.TEXT_EQ(injector.getName())};
    }

    @Nullable
    public String getTranscludeText() {
        if(transclude.isPresent()) {
            String text = transclude.get().getText();
            return (text.contains(":")) ? text.substring(text.indexOf(':')+1) : text;
        }
        return null;
    }

}
