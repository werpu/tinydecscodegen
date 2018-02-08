package supportive.fs;

import com.intellij.psi.PsiElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import supportive.reflectRefact.PsiWalkFunctions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static supportive.reflectRefact.PsiWalkFunctions.walkPsiTree;
import static supportive.utils.StringUtils.elVis;

@AllArgsConstructor
public class PsiElementContext {

    @Getter
    PsiElement element;


    public String getName() {
        return (String) elVis(element,"element", "name").orElse("");
    }

    public String getText() {
        return element.getText();
    }

    @Contract(
        pure = true
    )
    public int getTextLength() {
        return element.getTextLength();
    }

    @Contract(
        pure = true
    )
    public int getTextOffset() {
        return element.getTextOffset();
    }

    public PsiElementContext getRootElement() {
        PsiElement oldElement = null;
        PsiElement currElement = element;
        while(!currElement.toString().startsWith("JSFile")) {
            oldElement = currElement;
            currElement = currElement.getParent();
        }
        return new PsiElementContext(oldElement);
    }

    public List<PsiElementContext> findPsiElements(Function<PsiElement, Boolean> psiElementVisitor) {
        return findPsiElements(psiElementVisitor, false);
    }


    public Optional<PsiElementContext> findPsiElement(Function<PsiElement, Boolean> psiElementVisitor) {
        List<PsiElementContext> found = findPsiElements(psiElementVisitor, true);
        if(found.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(found.get(0));
        }
    }

    public List<PsiElementContext> getChildren(@Nullable Function<PsiElement, Boolean> psiElementVisitor) {
        final List<PsiElementContext> retVal = new LinkedList<>();
        if(element == null) {//not parseable
            return Collections.emptyList();
        }

        for(PsiElement el :element.getChildren()) {
            if(psiElementVisitor == null || psiElementVisitor.apply(el)) {
                retVal.add(new PsiElementContext(el));
            }
        }
        return retVal;
    }

    public List<PsiElementContext> walkParents(Function<PsiElement, Boolean> psiElementVisitor) {
        return PsiWalkFunctions.walkParents(getElement(), psiElementVisitor).stream()
                .map(PsiElementContext::new)
                .collect(Collectors.toList());
    }

    public Optional<PsiElementContext> walkParent(Function<PsiElement, Boolean> psiElementVisitor) {
        Optional<PsiElement> foundElement = PsiWalkFunctions.walkParent(getElement(),psiElementVisitor);
        if(foundElement.isPresent()) {
            return Optional.of(new PsiElementContext(foundElement.get()));
        }
        return Optional.empty();
    }

    protected List<PsiElementContext> findPsiElements(Function<PsiElement, Boolean> psiElementVisitor, boolean firstOnly) {
        final List<PsiElement> retVal;
        if(element == null) {//not parseable
            return Collections.emptyList();
        }

        retVal =  walkPsiTree(element, psiElementVisitor, firstOnly);
        return retVal.stream().map(el -> new PsiElementContext(el)).collect(Collectors.toList());
    }

}
