package supportive.fs.common;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Optional;

import static supportive.reflectRefact.PsiWalkFunctions.*;

public class ServiceContext extends AngularResourceContext {


    public ServiceContext(Project project, PsiFile psiFile, PsiElement element) {
        super(project, psiFile, element);
    }

    public ServiceContext(AnActionEvent event, PsiElement element) {
        super(event, element);
    }

    public ServiceContext(Project project, VirtualFile virtualFile, PsiElement element) {
        super(project, virtualFile, element);
    }

    public ServiceContext(IntellijFileContext fileContext, PsiElement element) {
        super(fileContext, element);
    }

    @Override
    protected void postConstruct() {
        super.postConstruct();
        Optional<PsiElementContext> serviceClass = resolveClass();
        Optional<PsiElementContext> psiServiceName = resolveName();
        clazzName = serviceClass.get().getName();
        artifactName = psiServiceName.isPresent() ? psiServiceName.get().getText() : clazzName;

        findParentModule();
    }

    private Optional<PsiElementContext> resolveName() {
        return resolveClass().get().$q(SERVICE_ANN, PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
    }

    @NotNull
    private Optional<PsiElementContext> resolveClass() {
        Optional<PsiElementContext> serviceClass = $q(SERVICE_CLASS).findFirst();
        if (!serviceClass.isPresent()) {
            throw new RuntimeException("Service class not found");
        }
        return serviceClass;
    }


    @Override
    public String getResourceName() {
        return getArtifactName();
    }

    @Override
    public Icon getIcon() {
        return AllIcons.Webreferences.Server;
    }
}
