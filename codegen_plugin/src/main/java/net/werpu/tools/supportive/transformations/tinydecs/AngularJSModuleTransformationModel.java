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
import lombok.Setter;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;
import net.werpu.tools.supportive.transformations.shared.ITransformationModel;
import net.werpu.tools.supportive.utils.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;

/**
 * A transformation context
 * for simple Angular JS Modules
 * <p>
 * The idea is
 * to get the neutral information which does not change
 * then the module declaration part with the name and the requires
 * and then the rest and then let a template transform this information
 * into an TN_DEC ng_module structure
 * with the required added backwards compatiblity (dont know yet
 * if there is a forward way for Angular2 as well, but I doubt it)
 */
@Getter
public class AngularJSModuleTransformationModel extends TypescriptFileContext implements ITransformationModel {
    public static final Object[] ANGULAR_MOD_DEF = {TreeQueryEngine.PARENTS_EQ_FIRST(TYPE_SCRIPT_VARIABLE), JS_CALL_EXPRESSION};
    /**
     * psi element covering the entire angular module defintion
     * aka angular.module('app.entry', ["bla","bla1"])
     * .component("remappedOverviews", new ApplicationsTable())
     * .component("remappedsChart", new ApplicationChart())
     * .component...
     *
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
     *
     * @param psiFile
     * @return
     */
    public static boolean isAffected(PsiFile psiFile) {
        return new IntellijFileContext(psiFile.getProject(), psiFile).$q(ANG1_MODULE_DCL).findFirst().isPresent();
    }

    /**
     * gets the text part which defines the begining of the file to the end of the imports
     *
     * @return the imports text
     */
    public String getImportText() {
        PsiElementContext lastImport = this.getLastImport().get();
        return this.getText().substring(0, lastImport.getTextRangeOffset() + lastImport.getTextLength() + 1);
    }

    /**
     * gets the text part from the imports to the beginning of the module declaration
     * including the var name =
     *
     * @return
     */
    public String getTextFromImportsToModuleDcl() {
        int importEnd = getImportEnd();
        Optional<PsiElementContext> varDcl = moduleDefStart.get().$q(TreeQueryEngine.PARENTS, JS_VAR_STATEMENT).findFirst();
        if (varDcl.isPresent()) {
            return this.getText().substring(importEnd, varDcl.get().getTextRangeOffset());
        }
        return this.getText().substring(importEnd, moduleDeclStart.get().getTextRangeOffset());
    }

    public String getModuleClassName() {
        return StringUtils.toCamelCase(getModuleName());
    }

    public String getDeclarationsPart() {
        if (moduleDefStart.isPresent()) {
            return this.getPsiFile().getText().substring(moduleDeclStart.get().getTextRangeOffset() + moduleDeclStart.get().getTextLength()).trim();
        }
        return "";
    }

    public String getRequiresAsString() {
        Optional<String> requires = this.requires.stream().map(el -> "\"" + el + "\"").reduce((el1, el2) -> el1 + ", " + el2);

        return requires.orElse("");
    }

    public String getLegacyName() {
        if (moduleDefStart.isPresent()) {
            return moduleDefStart.get().$q(TreeQueryEngine.PARENTS_EQ_FIRST(TYPE_SCRIPT_VARIABLE)).findFirst().get().getName();
        }
        return "legacy_" + this.moduleName;
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
            moduleName = nameLiteral.isPresent() ? nameLiteral.get().getUnquotedText() : "";
            requires = moduleDeclStart.get().$q(ANG1_MODULE_REQUIRES).map(el -> el.getUnquotedText()).collect(Collectors.toList());
            moduleDefStart = moduleDeclStart.get().$q(ANGULAR_MOD_DEF).findFirst();
        });
        lastImport = this.$q(ANY_TS_IMPORT).reduce((e1, e2) -> e2);
    }

    private int getImportEnd() {
        PsiElementContext lastImport = this.getLastImport().get();
        return lastImport.getTextRangeOffset() + lastImport.getTextLength() + 1;
    }

}
