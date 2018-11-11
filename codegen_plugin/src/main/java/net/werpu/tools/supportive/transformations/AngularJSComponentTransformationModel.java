package net.werpu.tools.supportive.transformations;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;
import net.werpu.tools.supportive.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;

/**
 * probably the most complicated context of all
 * the component context.
 * <p>
 * The aim for this is following
 * a) find out all the needed imports functions etc...
 * b) find out the component as and if not present use ctrl per default
 * c) Find out all the inlined functions and try to push them to the class level
 * d) Find out all the contextual information regarding the component injects
 * e) find the template reference and try to load the template
 * f) find out about all the watchers currently
 * <p>
 * Upon all this info we should make a transformation source which tries to transform the template
 * depending on the angular level (not part of this class, will be written later)
 * A simple replacer like we have it for the Module Transformation does not cut it anymore
 */
enum BindingType {
    INPUT, BOTH, ASTRING, FUNC, OPT_INPUT, OPT_BOTH, OPT_ASTRING, OPT_FUNC;

    public static BindingType translate(String in) {
        if (in.equals("<")) {
            return INPUT;
        } else if (in.equals("<?")) {
            return OPT_INPUT;
        } else if (in.equals("@")) {
            return ASTRING;
        } else if (in.equals("@?")) {
            return OPT_ASTRING;
        } else if (in.equals("=")) {
            return BOTH;
        } else if (in.equals("=?")) {
            return OPT_BOTH;
        } else if (in.equals("&")) {
            return FUNC;
        } else {
            return OPT_FUNC;
        }
    }
}

@Getter
@AllArgsConstructor
class BindingTypes {
    BindingType bindingType;
    String name;
}

@Getter
@AllArgsConstructor
class Injector {
    String name;
    String tsNameType;
}

public class AngularJSComponentTransformationModel extends TypescriptFileContext {

    public static final Object[] BINDINGS = {TYPE_SCRIPT_FIELD, NAME_EQ("bindings"), JS_OBJECT_LITERAL_EXPRESSION, JS_PROPERTY};
    public static final Object[] CONTROLLER_AS = {TYPE_SCRIPT_FIELD, NAME_EQ("controllerAs"), PSI_ELEMENT_JS_STRING_LITERAL};
    public static final Object[] CONTROLLER_ARR = {TYPE_SCRIPT_FIELD, NAME_EQ("controller"), JS_ARRAY_LITERAL_EXPRESSION};
    public static final Object[] STR_INJECTS = {CHILD_ELEM, JS_LITERAL_EXPRESSION, PSI_ELEMENT_JS_STRING_LITERAL};
    public static final Object[] CONTROLLER_FUNC = {CHILD_ELEM, TYPESCRIPT_FUNCTION_EXPRESSION};
    public static final Object[] PARAM_LISTS = {CONTROLLER_ARR, CONTROLLER_FUNC, CHILD_ELEM, TYPE_SCRIPT_PARAMETER_LIST, TYPE_SCRIPT_PARAM};

    @NotNull
    static Object[] COMPONENT_NAME(String className) {
        return new Object[]{TYPE_SCRIPT_NEW_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER, NAME_EQ(className), PARENTS, JS_ARGUMENTS_LIST, PSI_ELEMENT_JS_STRING_LITERAL};
    }


    Optional<PsiElementContext> lastImport;
    PsiElementContext rootBlock;
    String controllerAs;
    List<Injector> injects; //imports into the constructor
    String selectorName; //trace back into the module declaration for this component and then run our string dash transformation

    Optional<PsiElementContext> constructorDef;
    Optional<PsiElementContext> constructorBlock;

    List<PsiElementContext> inlineFunctions;
    String template; //original template after being found
    //List<PsiElementContext> watchers;
    List<BindingTypes> bindings;


    public AngularJSComponentTransformationModel(Project project, PsiFile psiFile, PsiElementContext rootBlock) {
        super(project, psiFile);
        applyRootBlock(psiFile, rootBlock);
    }

    public void applyRootBlock(PsiFile psiFile, PsiElementContext rootBlock) {
        if(rootBlock != null) {
            this.rootBlock = rootBlock;
        } else {
            this.rootBlock = new PsiElementContext(psiFile.getContext());
        }
    }

    public AngularJSComponentTransformationModel(AnActionEvent event, PsiElementContext rootBlock) {
        super(event);
        applyRootBlock(getPsiFile(), rootBlock);
    }

    public AngularJSComponentTransformationModel(Project project, VirtualFile virtualFile, PsiElementContext rootBlock) {
        super(project, virtualFile);
        applyRootBlock(getPsiFile(), rootBlock);
    }

    public AngularJSComponentTransformationModel(IntellijFileContext fileContext) {
        super(fileContext);
        applyRootBlock(getPsiFile(), rootBlock);
    }

    @Override
    protected void postConstruct() {
        super.postConstruct();
        parseImport();

        parseConstructor();

        parseSelectorName();
        parseInjects();
        parseBindings();

        parseInlineFunctions();

        parseTemplate();

    }

    private void parseTemplate() {
        Optional<PsiElementContext> returnStmt = rootBlock.$q(TYPE_SCRIPT_FIELD, NAME_EQ("template"), JS_RETURN_STATEMENT).findFirst();
        returnStmt.ifPresent((el) -> {
            Optional<PsiElementContext> found = Stream.concat(el.$q(PSI_ELEMENT_JS_STRING_LITERAL), el.$q(PSI_ELEMENT_JS_IDENTIFIER)).findFirst();
            if(found.isPresent() && found.get().getElement().toString().startsWith(PSI_ELEMENT_JS_IDENTIFIER)) {
                //identifier resolution
            } else if(found.isPresent() && found.get().getElement().getText().endsWith(".html")) {
                //file resolution
            } else if(found.isPresent()) {
                //string resolution
            }
        });

    }

    private void parseInlineFunctions() {
        inlineFunctions = constructorBlock.get().$q(TYPE_SCRIPT_FUNC_EXPR)
                .filter(expr -> !Strings.isNullOrEmpty(expr.getName()))
                .collect(Collectors.toList());
    }

    private void parseImport() {
        lastImport = this.$q(JS_ES_6_IMPORT_DECLARATION).reduce((e1, e2) -> e2);
    }

    private void parseConstructor() {
        constructorDef = rootBlock.$q(TYPE_SCRIPT_FIELD, NAME_EQ("controller")).findFirst();
        constructorBlock = constructorDef.get().$q(TYPE_SCRIPT_FUNC, JS_BLOCK_STATEMENT).findFirst();

    }

    private void parseInjects() {
        injects = new ArrayList<>();
        List<String> strInjects =
                rootBlock.$q(CONTROLLER_ARR, STR_INJECTS)
                .map(el-> el.getText())
                .collect(Collectors.toList());

        List<String> tsInjects = rootBlock.$q(PARAM_LISTS)
                .map(el -> el.getText())
                .collect(Collectors.toList());

        for(int cnt = 0; cnt < tsInjects.size(); cnt++) {
            String tsNameType = tsInjects.get(cnt);
            String injector = (cnt < strInjects.size() - 1) ? strInjects.get(cnt) : tsNameType.split("\\:")[0];
            injects.add(new Injector(injector, tsNameType));
        }

    }

    private void parseSelectorName() {
        String className = this.$q(TYPE_SCRIPT_CLASS).findFirst().map(el -> el.getName()).orElse("????");

        selectorName = super.findFirstUpwards(el -> {
            Optional<PsiElementContext> ctx = new IntellijFileContext(getProject(), el).$q(COMPONENT_NAME(className)).findFirst();
            if(!ctx.isPresent()) {
                return false;
            }
            //TODO check imports
            return true;

        }).stream()
                .flatMap(el -> el.$q(COMPONENT_NAME(className)))
                .map(el2 -> el2.getText())
                .map(compName -> StringUtils.toDash(compName))
                .findFirst().orElse("????");
    }

    private void parseControllerAs() {
        controllerAs = rootBlock.$q(CONTROLLER_AS)
                .map(el -> el.getText()).findFirst().orElse("ctrl");
    }

    private void parseBindings() {
        bindings = rootBlock.$q(BINDINGS).map(el -> {
            String propName = el.getName();
            Optional<PsiElementContext> first = el.$q(PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
            String type = (first.isPresent()) ? first.get().getText() : "<";
            return new BindingTypes(BindingType.translate(type), propName);
        }).collect(Collectors.toList());
    }
}