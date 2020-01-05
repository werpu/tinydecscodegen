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
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;
import net.werpu.tools.supportive.transformations.shared.ITransformationModel;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.ClassAttribute;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.ComponentBinding;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.FirstOrderFunction;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.Injector;
import net.werpu.tools.supportive.utils.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.ANY_TS_IMPORT;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.*;
import static net.werpu.tools.supportive.transformations.tinydecs.AngularJSComponentTransformationModel.*;

/**
 * Transformation model for the tiny decorations
 *
 * @See also AngularJSTransformationModel
 */
public class TinyDecsComponentTransformationModel extends TypescriptFileContext implements ITransformationModel {

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
    protected List<FirstOrderFunction> inlineFunctions;
    protected List<ClassAttribute> possibleClassAttributes;
    protected String template; //original template after being found
    protected List<ComponentBinding> bindings;
    protected String clazzName;
    protected List<PsiElementContext> attributes;

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
        parseAnnotation();
        parseSelector();
        parseControllerAs();
        parseTemplate();
       /* parseClassBlock();

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
        parseInlineClassAttributeCandidates();*/
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


    private boolean parseAnnotation() {
        Optional<PsiElementContext> decorator = rootBlock.$q(COMPONENT_ANN).findFirst();
        if(!decorator.isPresent()) {
            return true;
        }
        this.annotationBlock = decorator;
        return false;
    }

    void parseSelector() {

        Optional<PsiElementContext> selectorContext = annotationBlock.get().$q(JS_PROPERTY, PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("selector"), PARENT, JS_LITERAL_EXPRESSION, PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
        if(!selectorContext.isPresent()) {
            return;
        }
        selectorName = StringUtils.stripQuotes(selectorContext.get().getText());
    }

    void parseControllerAs() {
        Optional<PsiElementContext> controllerAsContext = annotationBlock.get().$q(JS_PROPERTY, PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("controllerAs"), PARENT, JS_LITERAL_EXPRESSION, PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
        if(!controllerAsContext.isPresent()) {
            this.controllerAs = "ctrl";
            return;
        }
        this.controllerAs = StringUtils.stripQuotes(controllerAsContext.get().getText());
    }



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

}
