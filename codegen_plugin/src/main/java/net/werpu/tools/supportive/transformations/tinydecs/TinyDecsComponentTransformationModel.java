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
import net.werpu.tools.supportive.transformations.shared.ITransformationModel;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.*;
import net.werpu.tools.supportive.utils.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.SUB_QUERY;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.*;
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
    protected List<PsiElementContext> destroyDef;

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
        parseClassBlock();
        parseComponentAnnotation();
        parseSelectorName();
        parseControllerAs();
        parseTemplate();

        parseBindings();
        parseInjects();
        parseAttributes();
        parseWatches();
       /* parseClassBlock();

        parseConstructor();

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

    /**
     * parse the component bindings for remapping
     */
    private void parseBindings() {
        this.bindings = rootBlock.$q(COMPONENT_ANN).filter(ann -> {
            PsiElementContext ident = ann.$q(JS_ES_6_DECORATOR, PSI_ELEMENT_JS_IDENTIFIER).reduce((el1, el2) -> el2).get();
            String text = ident.getText();
            return text.equals(ANN_INPUT) ||
                    text.equals(ANN_BOTH) ||
                    text.equals(ANN_FUNC) ||
                    text.equals(ANN_STRING);
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

            return new ComponentBinding(bindingType, identifier.getName(), type.isPresent() ? type.get().getText() : "any");
        }).collect(Collectors.toList());

    }

    /**
     * parse the injects for further processing
     */
    private void parseInjects() {
        this.injects = rootBlock.$q(TYPE_SCRIPT_FUNC, NAME_EQ("constructor"), DIRECT_CHILD(TYPE_SCRIPT_PARAMETER_LIST), TYPE_SCRIPT_PARAM)
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
}