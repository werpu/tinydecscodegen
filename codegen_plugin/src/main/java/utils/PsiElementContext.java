package utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class PsiElementContext {

    @Getter
    PsiElement element;


    public PsiElementContext getRootElement() {
        PsiElement oldElement = null;
        while(!element.toString().equals("JSFile")) {
            oldElement = element;
            element = element.getParent();
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

    protected List<PsiElementContext> findPsiElements(Function<PsiElement, Boolean> psiElementVisitor, boolean firstOnly) {
        final List<PsiElement> retVal = new LinkedList<>();
        if(element == null) {//not parseable
            return Collections.emptyList();
        }

        PsiRecursiveElementWalkingVisitor myElementVisitor = new PsiRecursiveElementWalkingVisitor() {

            public void visitElement(PsiElement element) {


                if (psiElementVisitor.apply(element)) {
                    retVal.add(element);
                    if(firstOnly) {
                        stopWalking();
                    }
                    return;
                }
                super.visitElement(element);
            }
        };

        myElementVisitor.visitElement(element);


        return retVal.stream().map(el -> new PsiElementContext(el)).collect(Collectors.toList());
    }

}
