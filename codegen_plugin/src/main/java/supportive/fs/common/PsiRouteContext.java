package supportive.fs.common;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.EqualsAndHashCode;
import lombok.Getter;



@EqualsAndHashCode(callSuper=false)
public class PsiRouteContext extends PsiElementContext implements IAngularFileContext {

    @Getter
    private final Route route;

    public PsiRouteContext(PsiElement element, Route route) {
        super(element);
        this.route = route;
    }

    @Override
    public String getDisplayName() {
        return route.getRouteKey();
    }

    @Override
    public String getResourceName() {
        return route.getRouteKey();
    }

    @Override
    public NgModuleFileContext getParentModule() {
        throw new RuntimeException("Not supported in this class");
    }

    @Override
    public VirtualFile getVirtualFile() {
        return getPsiFile().getVirtualFile();
    }

    @Override
    public PsiFile getPsiFile() {
        return element.getContainingFile();
    }


}
