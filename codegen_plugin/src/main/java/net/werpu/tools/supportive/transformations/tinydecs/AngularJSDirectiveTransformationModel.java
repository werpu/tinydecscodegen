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
import net.werpu.tools.supportive.transformations.shared.modelHelpers.GenericFunction;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.Injector;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.ParameterDeclaration;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.*;

/**
 * Directive code patterns, those are very similar to function based component definitions
 * (function based component definitions are in fact a subset of the directive based definitions)
 * <p>
 * TODO maybe another approach in this case is better
 * we might be able to just fetch all properties except for a handful of props
 * which need special treatment
 */
@Getter
public class AngularJSDirectiveTransformationModel extends AngularJSComponentTransformationModel {
    public static final Object[] DEFINITION_BLOCK = {JS_RETURN_STATEMENT, JS_OBJECT_LITERAL_EXPRESSION, FIRST};
    public static final Object[] TEMPLATE_DEF = {DEFINITION_BLOCK, CHILD_ELEM, JS_PROPERTY, NAME_EQ("template")};
    PsiElementContext outerDefintioon;
    List<GenericFunction> additionalFunctions;

    public AngularJSDirectiveTransformationModel(Project project, PsiFile psiFile, PsiElementContext rootBlock) {
        super(project, psiFile, rootBlock);
    }

    public AngularJSDirectiveTransformationModel(AnActionEvent event, PsiElementContext rootBlock) {
        super(event, rootBlock);
    }

    public AngularJSDirectiveTransformationModel(Project project, VirtualFile virtualFile, PsiElementContext rootBlock) {
        super(project, virtualFile, rootBlock);
    }

    public AngularJSDirectiveTransformationModel(IntellijFileContext fileContext) {
        super(fileContext);
    }

    @Override
    public String getRefactoredConstructorBlock() {
        String refactoredConstructorBlock = super.getRefactoredConstructorBlock().trim();
        return (refactoredConstructorBlock.substring(0, 1) + "\n" +
                this.getCodeBetweenDefinitionAndClassBlock() + "\n" +
                refactoredConstructorBlock.substring(1));
    }

    protected void parseClassBlock() {
        Stream<PsiElementContext> psiElementContextStream =
                rootBlock.$q(TYPE_SCRIPT_FUNC, TEXT_STARTS_WITH("export"))
                        .filter(el -> el.getText().contains("controller"));

        outerDefintioon = psiElementContextStream.findFirst().get();
        classBlock = outerDefintioon.$q(JS_RETURN_STATEMENT).findFirst().get();
    }

    @Override
    protected void parseBindings() {
        parseBindings(TN_DIRECTIVE_BINDINGS);
    }

    public String getFromImportsToClassDecl() {
        return rootBlock.getText().substring(lastImport.get().getTextRangeOffset() + lastImport.get().getTextLength() + 1, outerDefintioon.getTextRangeOffset());
    }

    public String getCodeBetweenDefinitionAndClassBlock() {
        return rootBlock.getText().substring(outerDefintioon.$q(JS_BLOCK_STATEMENT, FIRST).findFirst().get().getTextRangeOffset() + 1, classBlock.getTextRangeOffset());
    }

    @Override
    protected void postConstruct2() {
        super.postConstruct2();
        parseAdditionalFunctions();
    }

    private void parseAdditionalFunctions() {
        additionalFunctions = classBlock.$q(DEFINITION_BLOCK, CHILD_ELEM, JS_PROPERTY).map(el -> mapToFunction(el))
                .filter(el -> el != null)
                .collect(Collectors.toList());
        refactoriThisIntoFunctions();

    }

    @Override
    protected void parseRestrict() {
        restrict = "\"" + classBlock.$q(DEFINITION_BLOCK, JS_PROPERTY, NAME_EQ("restrict"), PSI_ELEMENT_JS_STRING_LITERAL)
                .map(el -> el.getUnquotedText()).findFirst().orElse("E") + "\"";
    }

    @Override
    protected void parsePriority() {
        priority = classBlock.$q(DEFINITION_BLOCK, JS_PROPERTY, NAME_EQ("priority"), PSI_ELEMENT_JS_NUMERIC_LITERAL)
                .map(el -> el.getText()).findFirst().orElse(null);
    }

    protected void parseTransclude() {
        transclude = classBlock.$q(DEFINITION_BLOCK, JS_PROPERTY, NAME_EQ("transclude")).findFirst();
    }

    @Nullable
    private GenericFunction mapToFunction(PsiElementContext el) {
        String propName = el.getName();
        Optional<PsiElementContext> func = el.$q(CHILD_ELEM, TYPESCRIPT_FUNCTION_EXPRESSION).findFirst();
        if (func.isPresent()) {

            List<ParameterDeclaration> parameters = func.get().$q(TYPE_SCRIPT_PARAMETER_LIST, FIRST, TYPE_SCRIPT_PARAM).map(
                    param -> new ParameterDeclaration(param)
            ).collect(Collectors.toList());

            return new GenericFunction(propName, parameters, func.get().$q(JS_BLOCK_STATEMENT).findFirst().get());
        }
        return null;
    }

    @Override
    protected void parseClassName() {
        clazzName = outerDefintioon.getName();
    }

    //we have to add
    @Override
    protected void parseInjects() {
        super.parseInjects();
        parseRootInjects();

    }

    private void parseRootInjects() {
        outerDefintioon.$q(CHILD_ELEM, TYPE_SCRIPT_PARAMETER_LIST, FIRST, TYPE_SCRIPT_PARAM)
                .forEach(param -> {
                    String paramName = param.getName();
                    Optional<PsiElementContext> psiParamType = param.$q(TYPE_SCRIPT_SINGLE_TYPE).findFirst();
                    String paramType = (psiParamType.isPresent()) ? psiParamType.get().getText() : "any";
                    Injector injector = new Injector(paramName, paramName + ": " + paramType);
                    if (!injects.contains(injector)) {
                        injects.add(injector);
                    }
                });
    }

    public void refactoriThisIntoFunctions() {

        //TODO we have to exclude the parameters we pass in from our
        //this list, the rest then can be thised.

        /*for (GenericFunction inlineFunction : additionalFunctions) {

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
        }*/
    }

    @Override
    protected void parseTemplate() {
        Optional<PsiElementContext> stringTemplate = classBlock.$q(TEMPLATE_DEF, ANY(new Object[]{JS_STRING_TEMPLATE_EXPRESSION}, new Object[]{PSI_ELEMENT_JS_STRING_LITERAL})).findFirst();
        Optional<PsiElementContext> refTemplate = classBlock.$q(TEMPLATE_DEF, PSI_ELEMENT_JS_IDENTIFIER).findFirst();

        if (stringTemplate.isPresent()) {
            PsiElementContext el = stringTemplate.get();
            template = "`" + el.getUnquotedText() + "`";
        } else if (refTemplate.isPresent()) {
            PsiElementContext el = refTemplate.get();
            template = "`" + el.getUnquotedText() + "`";
        } else {
            template = "``,//ERROR Template could not be resolved";
        }
    }

}
