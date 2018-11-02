package supportive.fs.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import supportive.refactor.DummyInsertPsiElement;
import supportive.refactor.RefactorUnit;
import supportive.reflectRefact.PsiWalkFunctions;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Streams.concat;
import static supportive.reflectRefact.IntellijRefactor.NG_MODULE;
import static supportive.reflectRefact.PsiWalkFunctions.*;
import static supportive.utils.StringUtils.elVis;


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
        clazzName = NgModuleFileGist.getFileData(psiFile).getClassName();
        artifactName =  NgModuleFileGist.getFileData(psiFile).getArtifactName();
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
        if(!clazz.isPresent()) {
            throw new RuntimeException("Module class not found");
        }
        return clazz.get();
    }




    protected void init() {

    }


    public String getModuleName() {
       return  NgModuleFileGist.getFileData(psiFile).getArtifactName();
    }


    public Optional<String> findClassName() {
        return  Optional.ofNullable(NgModuleFileGist.getFileData(psiFile).getClassName());
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
        paramSection. insertUpdateDefSection("exports", variableName);
    }

    public void appendProviders(String variableName) throws IOException {
        paramSection.insertUpdateDefSection("providers", variableName);
    }


    protected void findParentModule() {
        final PsiFile _this = this.psiFile;
        List<IntellijFileContext> modules = findFirstUpwards(psiFile -> {
            if(_this == psiFile) {
                return false;
            }
            return psiFile.getContainingFile().getText().contains(NG_MODULE);
        });
        this.parentModule = modules.isEmpty() ? null :  new NgModuleFileContext(modules.get(0));
    }

    @Override
    public String getResourceName() {
        return getModuleName();
    }
}
