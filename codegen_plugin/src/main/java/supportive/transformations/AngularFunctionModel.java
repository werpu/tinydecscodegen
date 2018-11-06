package supportive.transformations;

import lombok.Getter;
import supportive.fs.common.PsiElementContext;

import java.util.List;

@Getter

public class AngularFunctionModel {
    private final PsiElementContext element;
    private final PsiElementContext parentFunc;
    String name;

    List<String> parameterDeclarations;
    PsiElementContext functionBlock;

    public AngularFunctionModel(PsiElementContext element, PsiElementContext parentFunc) {
        this.element = element;
        this.parentFunc = parentFunc;
        postConstruct();
    }

    protected void postConstruct() {
        //add your parsing here
    }

}
