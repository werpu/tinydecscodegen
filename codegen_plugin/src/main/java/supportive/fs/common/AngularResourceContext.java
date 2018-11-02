package supportive.fs.common;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Getter;

import java.util.List;

import static supportive.reflectRefact.IntellijRefactor.NG_MODULE;

public abstract class AngularResourceContext extends TypescriptResourceContext implements IAngularFileContext {
    @Getter
    protected String clazzName;
    @Getter
    protected NgModuleFileContext parentModule;
    @Getter
    protected String artifactName;

    public AngularResourceContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public AngularResourceContext(AnActionEvent event) {
        super(event);
    }

    public AngularResourceContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public AngularResourceContext(IntellijFileContext fileContext) {
        super(fileContext);
    }

    public AngularResourceContext(Project project, PsiFile psiFile, PsiElement psiElement) {
        super(project, psiFile, psiElement);
    }

    public AngularResourceContext(AnActionEvent event, PsiElement psiElement) {
        super(event, psiElement);
    }

    public AngularResourceContext(Project project, VirtualFile virtualFile, PsiElement psiElement) {
        super(project, virtualFile, psiElement);
    }

    public AngularResourceContext(IntellijFileContext fileContext, PsiElement psiElement) {
        super(fileContext, psiElement);
    }


    protected void findParentModule() {
        List<IntellijFileContext> modules = findFirstUpwards(psiFile -> psiFile.getContainingFile().getText().contains(NG_MODULE));
        this.parentModule = modules.isEmpty() ? null :  new NgModuleFileContext(modules.get(0));
    }

    public String getDisplayName() {
        String finalArtifactName = Strings.nullToEmpty(artifactName);
        String finalModuleName = parentModule != null ? parentModule.getModuleName() : "";
        return finalArtifactName+" ["+finalModuleName+"]";
    }

    


}
