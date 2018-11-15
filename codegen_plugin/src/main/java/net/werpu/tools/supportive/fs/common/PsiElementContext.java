package net.werpu.tools.supportive.fs.common;

import com.intellij.psi.PsiElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
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

    public List<PsiElementContext> getChildren(@Nullable Function<PsiElement, Boolean> psiElementVisitor) {
        final List<PsiElementContext> retVal = new LinkedList<>();
        if (element == null) {//not parseable
            return emptyList();
        }

        for (PsiElement el : element.getChildren()) {
            if (psiElementVisitor == null || psiElementVisitor.apply(el)) {
                retVal.add(new PsiElementContext(el));
            }
        }
        return retVal;
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


    public List<PsiElementContext> walkParents(Function<PsiElement, Boolean> psiElementVisitor) {
        return PsiWalkFunctions.walkParents(getElement(), psiElementVisitor).stream()
                .map(PsiElementContext::new)
                .collect(Collectors.toList());
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

    public Stream<PsiElementContext> queryContent(Object... items) {
        return PsiWalkFunctions.queryContent(this.getElement(), items);
    }

    public Stream<PsiElementContext> $q(Object... items) {
        return PsiWalkFunctions.queryContent(this.getElement(), flattendArr(items).stream().toArray(Object[]::new));
    }

    public List<Object> flattendArr(Object[] items) {
        List<Object> retList = new LinkedList<>();
        for(Object item: items) {
            if(item.getClass().isArray()) {
                retList.addAll(flattendArr((Object[]) item));
            } else {
                retList.add(item);
            }
        }
        return retList;
    }


    public Stream<PsiElementContext> $q(Object[] items, Object... items2) {
        return PsiWalkFunctions.queryContent(this.getElement(), ArrayUtils.addAll(flattendArr(items).toArray(), flattendArr(items2).toArray()));
    }

    public Stream<PsiElementContext> $q(Object[]... items) {
        Object[] all = Arrays.stream(items).flatMap(item -> flattendArr(item).stream())
                .toArray();
        return PsiWalkFunctions.queryContent(this.getElement(), all);
    }

    public List<PsiElementContext> getImportIdentifiers(String varToCheck) {
        return this.queryContent(JS_ES_6_IMPORT_DECLARATION, JS_ES_6_IMPORT_SPECIFIER, PSI_ELEMENT_JS_IDENTIFIER, "TEXT:(" + varToCheck + ")").collect(Collectors.toList());
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
