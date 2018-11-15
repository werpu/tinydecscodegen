package net.werpu.tools.supportive.transformations;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import lombok.Setter;
import net.werpu.tools.factories.TnDecGroupFactory;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;
import net.werpu.tools.supportive.utils.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;

/**
 * A transformation context
 * for simple Angular JS Modules
 *
 * The idea is
 * to get the neutral information which does not change
 * then the module declaration part with the name and the requires
 * and then the rest and then let a template transform this information
 * into an TN_DEC ng_module structure
 * with the required added backwards compatiblity (dont know yet
 * if there is a forward way for Angular2 as well, but I doubt it)
 */
@Getter
public class AngularJSModuleTransformationModel extends TypescriptFileContext {


    public static final Object[] ANGULAR_MOD_DEF = {PARENTS_EQ_FIRST(TYPE_SCRIPT_VARIABLE), JS_CALL_EXPRESSION};

    /**
     * psi element covering the entire angular module defintion
     * aka angular.module('app.entry', ["bla","bla1"])
     *     .component("remappedOverviews", new ApplicationsTable())
     *     .component("remappedsChart", new ApplicationChart())
     *     .component...
     * @return
     */
    Optional<PsiElementContext> moduleDefStart;
    /**
     * covering only the module declaration part
     * aka angular.module('app.entry', ["bla","bla1"]);
     */
    Optional<PsiElementContext> moduleDeclStart;

    /**
     * the module name
     */
    String moduleName;

    /**
     * the position of the last import
     */
    Optional<PsiElementContext> lastImport;

    /**
     * the list of requires defined in the module
     */
    List<String> requires;


    @Setter
    boolean applicationBootstrap;


    public AngularJSModuleTransformationModel(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public AngularJSModuleTransformationModel(AnActionEvent event) {
        super(event);
    }

    public AngularJSModuleTransformationModel(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public AngularJSModuleTransformationModel(IntellijFileContext fileContext) {
        super(fileContext);
    }



    /**
     * helper to determine the AngularJS artifact part
     * @param psiFile
     * @return
     */
    public static boolean isAffected(PsiFile psiFile) {
        return new IntellijFileContext(psiFile.getProject(), psiFile).$q(ANG1_MODULE_DCL).findFirst().isPresent();
    }

    /**
     * gets the text part which defines the begining of the file to the end of the imports
     * @return the imports text
     */
    public String getImportText() {
        PsiElementContext lastImport = this.getLastImport().get();
        return this.getText().substring(0, lastImport.getTextOffset()+lastImport.getTextLength()+1);
    }

    /**
     * gets the text part from the imports to the beginning of the module declaration
     * including the var name =
     * @return
     */
    public String getTextFromImportsToModuleDcl() {
        int importEnd = getImportEnd();
        Optional<PsiElementContext> varDcl = moduleDefStart.get().$q(P_PARENTS, JS_VAR_STATEMENT).findFirst();
        if(varDcl.isPresent()) {
            return this.getText().substring(importEnd, varDcl.get().getTextOffset());
        }
        return this.getText().substring(importEnd, moduleDeclStart.get().getTextOffset());
    }

    public String getModuleClassName() {
        return StringUtils.toCamelCase(getModuleName());
    }

    public String getDeclarationsPart() {
        if(moduleDefStart.isPresent()) {
            return this.getPsiFile().getText().substring(moduleDeclStart.get().getTextOffset()+moduleDeclStart.get().getTextLength()).trim();
        }
        return "";
    }

    public String getRequiresAsString() {
        Optional<String> requires = this.requires.stream().map(el -> "\"" + el + "\"").reduce((el1, el2) -> el1 + ", " + el2);

        return requires.orElse("");
    }

    public String getLegacyName() {
        if(moduleDefStart.isPresent()) {
            return moduleDefStart.get().$q(PARENTS_EQ_FIRST(TYPE_SCRIPT_VARIABLE)).findFirst().get().getName();
        }
        return "legacy_"+this.moduleName;
    }

    /**
     * parsing part, parse the various contextual parts of the module file
     * into separate entities, which we can process later
     */
    @Override
    protected void postConstruct() {
        super.postConstruct();


        moduleDeclStart = this.$q(ANG1_MODULE_DCL).findFirst();
        moduleDeclStart.ifPresent(psiElementContext -> {
            Optional<PsiElementContext> nameLiteral = psiElementContext.$q(ANG1_MODULE_NAME).findFirst();
            moduleName = nameLiteral.isPresent() ? nameLiteral.get().getText() : "";
            requires = moduleDeclStart.get().$q(ANG1_MODULE_REQUIRES).map(el -> el.getText()).collect(Collectors.toList());
            moduleDefStart = moduleDeclStart.get().$q(ANGULAR_MOD_DEF).findFirst();
        });
        lastImport = this.$q(JS_ES_6_IMPORT_DECLARATION).reduce((e1, e2) -> e2);
    }

    private int getImportEnd() {
        PsiElementContext lastImport = this.getLastImport().get();
        return lastImport.getTextOffset() + lastImport.getTextLength() + 1;
    }






}
