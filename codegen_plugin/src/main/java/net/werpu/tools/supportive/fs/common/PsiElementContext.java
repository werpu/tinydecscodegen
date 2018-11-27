package net.werpu.tools.supportive.fs.common;

import com.intellij.psi.PsiElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.supportive.refactor.IRefactorUnit;
import net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;
import net.werpu.tools.supportive.utils.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.utils.StringUtils.elVis;

@AllArgsConstructor
public class PsiElementContext {

    @Getter
    PsiElement element;


    public String getName() {
        return (String) elVis(element, "name").orElse("");
    }

    public String getText() {
        String text = element.getText();
        text = text.replaceAll("^[\\\"\\'](.*)[\\\"\\']$", "$1");
        return text;
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
        while (!currElement.toString().startsWith("JSFile")) {
            oldElement = currElement;
            currElement = currElement.getParent();
        }
        return new PsiElementContext(oldElement);
    }

    public PsiElementContext getParent() {
        PsiElement parent = element.getParent();
        return parent != null ? new PsiElementContext(parent) : null;
    }

    public List<PsiElementContext> findPsiElements(Function<PsiElement, Boolean> psiElementVisitor) {
        return findPsiElements(psiElementVisitor, false);
    }


    public Optional<PsiElementContext> findPsiElement(Function<PsiElement, Boolean> psiElementVisitor) {
        List<PsiElementContext> found = findPsiElements(psiElementVisitor, true);
        if (found.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(found.get(0));
        }
    }

    public List<PsiElementContext> findPsiElements2(Function<PsiElementContext, Boolean> psiElementVisitor) {
        return findPsiElements2(psiElementVisitor, false);
    }

    /*public List<PsiElementContext> getChildren(@Nullable Function<PsiElement, Boolean> psiElementVisitor) {

        if (element == null) {//not parseable
            return emptyList();
        }

        return Arrays.stream(element.getChildren())
                .filter(el -> psiElementVisitor == null || psiElementVisitor.apply(el))
                .map(PsiElementContext::new)
                .collect(Collectors.toList());
    }*/

    public List<PsiElementContext> getChildren(@Nullable Function<PsiElementContext, Boolean> psiElementVisitor) {

        if (element == null) {//not parseable
            return emptyList();
        }

        PsiElement[] children = element.getChildren();
        //seems like a bug in the api, getChildren returns an empty array
        //first child works however
        if(children.length == 0 && element.getFirstChild() != null)  {
            children = new PsiElement[] {element.getFirstChild()};
        }
        return Arrays.stream(children)
                .map(PsiElementContext::new)
                .filter(el -> psiElementVisitor == null || psiElementVisitor.apply(el))
                .collect(Collectors.toList());
    }

    /**
     * all parents
     * @return all parents of the current element
     */
    public List<PsiElementContext> parents() {
        return this.walkParents(el -> true);
    }

    /**
     * single parent
     * @return a single parent as list or empty if none is found
     */
    public List<PsiElementContext> parent() {
        return this.walkParent(el -> true)
                .map(Arrays::asList).orElse(emptyList());
    }


    /**
     * n parents
     */
    public List<PsiElementContext> parents(int n) {
        List<PsiElementContext> allParents = this.walkParents(el -> true);
        return allParents.subList(0, Math.min(n, allParents.size()));
    }


    public List<PsiElementContext> walkParents(Function<PsiElementContext, Boolean> psiElementVisitor) {
        return PsiWalkFunctions.walkParents(this, psiElementVisitor);
    }



    public Optional<PsiElementContext> walkParent(Function<PsiElement, Boolean> psiElementVisitor) {
        Optional<PsiElement> foundElement = PsiWalkFunctions.walkParent(getElement(), psiElementVisitor);
        if (foundElement.isPresent()) {
            return Optional.of(new PsiElementContext(foundElement.get()));
        }
        return Optional.empty();
    }

    protected List<PsiElementContext> findPsiElements(Function<PsiElement, Boolean> psiElementVisitor, boolean firstOnly) {
        final List<PsiElement> retVal;
        if (element == null) {//not parseable
            return emptyList();
        }

        retVal = walkPsiTree(element, psiElementVisitor, firstOnly);
        return retVal.stream().map(el -> new PsiElementContext(el)).collect(Collectors.toList());
    }

    protected List<PsiElementContext> findPsiElements2(Function<PsiElementContext, Boolean> psiElementVisitor, boolean firstOnly) {
        final List<PsiElement> retVal;
        if (element == null) {//not parseable
            return emptyList();
        }

        retVal = walkPsiTree(element, (psiElement -> psiElementVisitor.apply(new PsiElementContext(psiElement))), firstOnly);
        return retVal.stream().map(el -> new PsiElementContext(el)).collect(Collectors.toList());
    }

    public Stream<PsiElementContext> queryContent(Object... items) {
        return PsiWalkFunctions.queryContent(this.getElement(), items);
    }

    public Stream<PsiElementContext> $q(Object... items) {
        return PsiWalkFunctions.queryContent(this.getElement(), items);
    }


    public List<PsiElementContext> getImportIdentifiers(String varToCheck) {
        return this.queryContent(JS_ES_6_IMPORT_DECLARATION, JS_ES_6_IMPORT_SPECIFIER, PSI_ELEMENT_JS_IDENTIFIER, TreeQueryEngine.TEXT_EQ( varToCheck )).collect(Collectors.toList());
    }

    public String calculateRefactoring(List<IRefactorUnit> refactorings) {
        if (refactorings.isEmpty()) {
            return this.element.getText();
        }
        //all refactorings must be of the same vFile TODO add check here
        String toSplit = this.element.getText();
        return StringUtils.refactor(refactorings, toSplit);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsiElementContext that = (PsiElementContext) o;
        return Objects.equals(element.getTextOffset(), that.element.getTextOffset()) && Objects.equals(element.getText(), that.element.getText());
    }

    @Override
    public int hashCode() {
        return element.getTextOffset() * 100000 + element.getText().hashCode();
    }
}
