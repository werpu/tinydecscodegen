package supportive.fs.common;

import com.google.common.collect.Streams;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Streams.concat;
import static supportive.reflectRefact.IntellijRefactor.NG_MODULE;
import static supportive.reflectRefact.PsiWalkFunctions.*;

public class FilterPipeContext extends AngularResourceContext {
    public FilterPipeContext(Project project, PsiFile psiFile, PsiElement element) {
        super(project, psiFile, element);
    }

    public FilterPipeContext(AnActionEvent event, PsiElement element) {
        super(event, element);
    }

    public FilterPipeContext(Project project, VirtualFile virtualFile, PsiElement element) {
        super(project, virtualFile, element);
    }

    public FilterPipeContext(IntellijFileContext fileContext, PsiElement element) {
        super(fileContext, element);
    }

    @Override
    protected void postConstruct() {
        super.postConstruct();
        Optional<PsiElementContext> clazz = concat($q(FILTER_CLASS), $q(PIPE_CLASS))
                .findFirst();
        if(!clazz.isPresent()) {
            throw new RuntimeException("Filter class not found");
        }


        Optional<PsiElementContext> thePsiArtifactName = clazz.get().$q(SERVICE_ANN, PSI_ELEMENT_JS_STRING_LITERAL).findFirst();

        clazzName = clazz.get().getName();
        artifactName = thePsiArtifactName.isPresent() ? thePsiArtifactName.get().getText() : clazzName;

        findParentModule();
    }


    public String getDisplayName() {
        return "TODO";
    }

    @Override
    public String getResourceName() {
        return getArtifactName();
    }
}
