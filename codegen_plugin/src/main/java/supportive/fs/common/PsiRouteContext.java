package supportive.fs.common;

import com.intellij.psi.PsiElement;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper=false)
public class PsiRouteContext extends PsiElementContext {

    @Getter
    private final Route route;

    public PsiRouteContext(PsiElement element, Route route) {
        super(element);
        this.route = route;
    }



}
