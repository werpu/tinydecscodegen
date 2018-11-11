package net.werpu.tools.supportive.fs.common;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import javax.swing.*;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.DTO_CLASS;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.TYPE_SCRIPT_CLASS;

public class DtoContext extends AngularResourceContext {
    public DtoContext(Project project, PsiFile psiFile, PsiElement element) {
        super(project, psiFile, element);
    }

    public DtoContext(AnActionEvent event, PsiElement element) {
        super(event, element);
    }

    public DtoContext(Project project, VirtualFile virtualFile, PsiElement element) {
        super(project, virtualFile, element);
    }

    public DtoContext(IntellijFileContext fileContext, PsiElement element) {
        super(fileContext, element);
    }


    @Override
    protected void postConstruct() {
        super.postConstruct();

        resourceRoot = resolveClass();
        clazzName = resolveClass().getName();
        artifactName =  clazzName;

        findParentModule();
    }

    private PsiElementContext resolveClass() {
        String name = $q(DTO_CLASS).findFirst().get().getName();
        return $q(TYPE_SCRIPT_CLASS)
                .filter(clz -> name.startsWith(clz.getName()) && name.length() > clz.getName().length())
                .findFirst()
                .get();
    }

    @Override
    public String getResourceName() {
        return getArtifactName();
    }

    @Override
    public Icon getIcon() {
        return AllIcons.Nodes.Jsf.ManagedBean;
    }


}
