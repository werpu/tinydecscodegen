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

package net.werpu.tools.supportive.fs.common;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import net.werpu.tools.supportive.fs.common.errors.ResourceClassNotFound;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static net.werpu.tools.supportive.reflectRefact.IntellijRefactor.NG_MODULE;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.MODULE_CLASS;

/**
 * placeholder for a module file context
 * <p>
 * this is a context which basically implements
 * all the required add operations for our artifacts, including
 * also the includes statement and omitting double insers
 */
public class NgModuleFileContext extends AngularResourceContext {
    AssociativeArraySection paramSection;

    public NgModuleFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public NgModuleFileContext(AnActionEvent event) {
        super(event);
    }

    public NgModuleFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public NgModuleFileContext(IntellijFileContext fileContext) {
        super(fileContext);
    }

    @Override
    protected void postConstruct() {
        super.postConstruct();

        NgModuleFileGist.init();

        resourceRoot = NgModuleFileGist.getResourceRoot(psiFile);
        AngularArtifactGist fileData = NgModuleFileGist.getFileData(psiFile);

        clazzName = fileData.getClassName();
        artifactName = fileData.getArtifactName();
        paramSection = resolveParameters();

        findParentModule();
    }

    @NotNull
    private AssociativeArraySection resolveParameters() {
        return NgModuleFileGist.resolveParameters(psiFile);
    }

    @NotNull
    public PsiElementContext resolveClass(PsiFile psiFile) {
        Optional<PsiElementContext> clazz = new PsiElementContext(psiFile).$q(MODULE_CLASS).findFirst();
        if (!clazz.isPresent()) {
            throw new RuntimeException("Module class not found");
        }
        return clazz.get();
    }

    protected void init() {

    }

    public String getModuleName() {
        AngularArtifactGist fileData = NgModuleFileGist.getFileData(psiFile);
        if (fileData == null) {
            return "";
        }
        return fileData.getArtifactName();
    }

    public Optional<String> findClassName() {
        return Optional.ofNullable(NgModuleFileGist.getFileData(psiFile).getClassName());
    }

    public void appendDeclaration(String variableName) throws IOException {
        //First add the needed declarational part if missing
        //then sync the code back in
        //then add the declaration into the declarational part
        paramSection.insertUpdateDefSection("declarations", variableName);
    }

    public void appendImports(String variableName) throws IOException {
        paramSection.insertUpdateDefSection("imports", variableName);

    }

    public void appendExports(String variableName) throws IOException {
        paramSection.insertUpdateDefSection("exports", variableName);
    }

    public void appendProviders(String variableName) throws IOException {
        paramSection.insertUpdateDefSection("providers", variableName);
    }

    protected void findParentModule() {
        try {
            final PsiFile _this = this.psiFile;
            List<IntellijFileContext> modules = findFirstUpwards(psiFile -> {
                if (_this == psiFile) {
                    return false;
                }
                return psiFile.getContainingFile().getText().contains(NG_MODULE);
            });
            this.parentModule = modules.isEmpty() ? null : new NgModuleFileContext(modules.get(0));
        } catch (ResourceClassNotFound ex) {
            this.parentModule = null;
        }
    }

    @Override
    public String getResourceName() {
        return getModuleName();
    }

    public Icon getIcon() {
        return AllIcons.Javaee.EjbModule;
    }
}
