package supportive.fs.common;

import com.intellij.psi.PsiElement;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static supportive.reflectRefact.PsiWalkFunctions.JS_ES_6_FROM_CLAUSE;
import static supportive.reflectRefact.PsiWalkFunctions.PSI_ELEMENT_JS_IDENTIFIER;
import static supportive.reflectRefact.PsiWalkFunctions.PSI_ELEMENT_JS_STRING_LITERAL;

public class PsiRouteContext extends PsiElementContext {

    @Getter
    private final Route route;

    public PsiRouteContext(PsiElement element, Route route) {
        super(element);
        this.route = route;
    }



}
