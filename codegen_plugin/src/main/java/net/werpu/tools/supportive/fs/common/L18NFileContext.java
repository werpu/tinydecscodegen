package net.werpu.tools.supportive.fs.common;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.yourkit.util.Strings;
import lombok.Getter;
import net.werpu.tools.supportive.reflectRefact.navigation.UnaryCommand;
import net.werpu.tools.supportive.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.NEXT_SIBLINGS;


/**
 * Parsing context for L18NFiles
 * <p>
 * We need this to be able to process L18NFiles properly
 */
public class L18NFileContext extends IntellijFileContext {

    @Getter
    PsiElementContext resourceRoot;

    public L18NFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);

    }

    protected void postConstruct() {
        super.postConstruct();
        resourceRoot = new PsiElementContext(getPsiFile()).$q(JSON_OBJECT).findFirst().get();
    }

    public L18NFileContext(AnActionEvent event) {
        super(event);
    }

    public L18NFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public L18NFileContext(IntellijFileContext fileContext) {
        super(fileContext.getProject(), fileContext.getPsiFile());
    }


    public Icon getIcon() {
        return AllIcons.Nodes.Artifact;
    }

    /**
     * fetches a value
     *
     * @param key either a single key or a nested with . divided key path
     * @return
     */
    @NotNull
    public Optional<PsiElementContext> getValue(String key) {
        if (key.contains(".")) {
            String[] keys = key.split("\\.");
            return getValue(keys);
        }
        return resourceRoot.$q(DIRECT_CHILD(JSON_PROPERTY), DIRECT_CHILD(JSON_STRING_LITERAL), TEXT_EQ(key)).findFirst();
    }

    /**
     * deep search
     *
     * @param key        a single key currently no . searches are allowed in case of a searchDeep == true
     * @param searchDeep if true a deep search is performed until a key is hit
     * @return
     */
    @NotNull
    public Optional<PsiElementContext> getValue(String key, boolean searchDeep) {
        if (!searchDeep) {
            return this.getValue(key);
        }

        return resourceRoot.$q(DIRECT_CHILD(JSON_PROPERTY), DIRECT_CHILD(JSON_STRING_LITERAL), TEXT_EQ(key)).findFirst();
    }

    //deeper nesting queries with exact matches

    /**
     * a precise search where a set of keys needs to match
     *
     * @param keys
     * @return
     */
    @NotNull
    public Optional<PsiElementContext> getValue(String... keys) {

        List<Object> query = new LinkedList<>();
        //query.add(CHILD_ELEM);
        for (int cnt = 0; cnt < keys.length; cnt++) {
            String key = keys[cnt];
            if (cnt < keys.length - 1) {
                query.addAll(asList(CHILD_ELEM, CHILD_ELEM, JSON_STRING_LITERAL, TEXT_EQ(key),  NEXT_SIBLINGS(JSON_OBJECT)));
            } else {
                query.addAll(asList(CHILD_ELEM, CHILD_ELEM, JSON_STRING_LITERAL, TEXT_EQ(key), ANY(NEXT_SIBLINGS(JSON_OBJECT), NEXT_SIBLINGS(JSON_STRING_LITERAL))));
            }
        }


        return resourceRoot.$q(query.stream().toArray(Object[]::new)).findFirst();
    }

    public Optional<String> getValueAsStr(String... keys) {
        Optional<PsiElementContext> retVal = getValue(keys);

        if (retVal.isPresent()) {
            return Optional.of(StringUtils.stripQuotes(retVal.get().getText()));
        }
        return Optional.empty();
    }

    /**
     * fetches the key for a given element String, in this case it is pointless to retun
     * psi elements because we often have to combine keys as well into one
     *
     * @param value
     * @return a list of possible entries, in case nothing is found we get an empty list back‚
     */
    @NotNull
    public List<String> getKey(String value) {
        Stream<PsiElementContext> foundElements = resourceRoot.queryContent(JSON_STRING_LITERAL, TEXT_EQ(value));
        return foundElements.map(keyMapper())
                .filter(Optional::isPresent)
                .map(el -> flattenHierarchy(el.get()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * flattens the hierarchy into a usual <key>.<key>.<key> hierarchy
     * the starting point usually is a deep node, hence we must inverse the result‚
     *
     * @param el
     * @return
     */
    @NotNull
    private Optional<String> flattenHierarchy(PsiElementContext el) {
        return el.$q(PARENTS_EQ(JSON_PROPERTY))
                .map(el2 -> el2.$q(JSON_STRING_LITERAL).findFirst())
                .filter(el2 -> el2.isPresent())
                .map(el2 -> StringUtils.stripQuotes(el2.get().getText()))
                .reduce((el1, el2) -> {
                    if (Strings.isNullOrEmpty(el2)) return el1;
                    return el2 + "." + el1;
                });


    }

    /**
     * filter method which checks if a current element is a key in our L18N json file
     *
     * @return
     */
    @NotNull
    private Function<PsiElementContext, Optional<PsiElementContext>> keyMapper() {
        return el -> {
            //only keys are taken into consideration‚
            PsiElementContext parent = el.getParent();
            if (!isProperty(parent)) return Optional.empty();
            //key must be different
            PsiElementContext key = parent.$q(DIRECT_CHILD(JSON_STRING_LITERAL)).findFirst().get();

            if (StringUtils.literalEquals(key.getText(), el.getText())) {
                return Optional.empty();
            }
            return Optional.ofNullable(key);
        };
    }

    private boolean isProperty(PsiElementContext parent) {
        String simpleName = parent.getElement().getClass().getSimpleName();
        if (simpleName.startsWith(JSON_PROPERTY)) {
            return true;
        }
        return false;
    }


}
