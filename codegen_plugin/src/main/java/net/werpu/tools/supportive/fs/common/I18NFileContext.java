/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.supportive.fs.common;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import lombok.Getter;
import net.werpu.tools.supportive.refactor.IRefactorUnit;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.transformations.i18n.I18NEntry;
import net.werpu.tools.supportive.utils.IntellijUtils;
import net.werpu.tools.supportive.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.*;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.smartInvokeLater;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.writeTransaction;


/**
 * Parsing context for L18NFiles
 * <p>
 * We need this to be able to process L18NFiles properly
 */
public class I18NFileContext extends IntellijFileContext {

    @Getter
    PsiElementContext resourceRoot;

    PsiI18nEntryContext entryContext;

    @Getter
    I18NEntry tree;

    @Getter
    private List<IRefactorUnit> refactorUnits = Lists.newArrayList();

    @Getter
    private String refactoredText;


    public I18NFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);

    }

    protected void postConstruct() {
        super.postConstruct();

        PsiElementContext psiElementContext = new PsiElementContext(getPsiFile());
        Optional<PsiElementContext> first = psiElementContext.$q(JSON_OBJECT).findFirst();
        if(first.isPresent()) {
            resourceRoot = first.get();
        } else {
            resourceRoot = psiElementContext.$q(JS_OBJECT_LITERAL_EXPRESSION).findFirst().get();
        }
    }

    public I18NFileContext(AnActionEvent event) {
        super(event);
    }

    public I18NFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public I18NFileContext(IntellijFileContext fileContext) {
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

        //TODO move this functionality into the Entry context ion the long run
        if(isTS()) {
            return resourceRoot.$q(DIRECT_CHILD(JS_PROPERTY), NAME_EQ(key), ANY(DIRECT_CHILD(JS_LITERAL_EXPRESSION), DIRECT_CHILD(PSI_ELEMENT_JS_STRING_LITERAL))).findFirst();
        } else {
            return resourceRoot.$q(DIRECT_CHILD(JSON_PROPERTY), DIRECT_CHILD(JSON_STRING_LITERAL), TEXT_EQ(key), ANY(NEXT_SIBLINGS(JSON_OBJECT), NEXT_SIBLINGS(JSON_STRING_LITERAL))).findFirst();
        }
    }

    private boolean isTS() {
        return IntellijUtils.isTypescript(resourceRoot.getElement().getLanguage().getAssociatedFileType());
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


        if(isTS()) {
            return resourceRoot.$q(JS_PROPERTY, NAME_EQ(key), ANY(DIRECT_CHILD(JS_LITERAL_EXPRESSION), DIRECT_CHILD(PSI_ELEMENT_JS_STRING_LITERAL))).findFirst();
        } else {
            return resourceRoot.$q(JSON_PROPERTY, DIRECT_CHILD(JSON_STRING_LITERAL), TEXT_EQ(key), ANY(NEXT_SIBLINGS(JSON_OBJECT), NEXT_SIBLINGS(JSON_STRING_LITERAL))).findFirst();
        }
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
                if(isTS()) {
                    query.addAll(asList(JS_PROPERTY, NAME_EQ(key), DIRECT_CHILD(JS_OBJECT_LITERAL_EXPRESSION)));
                } else {
                    query.addAll(asList(CHILD_ELEM, CHILD_ELEM, JSON_STRING_LITERAL, TEXT_EQ(key),  NEXT_SIBLINGS(JSON_OBJECT)));
                }
            } else {
                if(isTS()) {
                    query.addAll(asList(JS_PROPERTY, NAME_EQ(key), DIRECT_CHILD(JS_OBJECT_LITERAL_EXPRESSION)));

                    query.addAll(asList(CHILD_ELEM, JS_PROPERTY, NAME_EQ(key), ANY(DIRECT_CHILD(JS_LITERAL_EXPRESSION), DIRECT_CHILD(PSI_ELEMENT_JS_STRING_LITERAL))));
                } else {
                    query.addAll(asList(CHILD_ELEM, CHILD_ELEM, JSON_STRING_LITERAL, TEXT_EQ(key), ANY(NEXT_SIBLINGS(JSON_OBJECT), NEXT_SIBLINGS(JSON_STRING_LITERAL))));
                }
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

    public void setText(String text) throws IOException {
        this.refactoredText = text;
        virtualFile.setBinaryContent(text.getBytes(virtualFile.getCharset()));
    }

    //TODO externalize this or move it into the base class
    public void addRefactoring(RefactorUnit unit) {
        this.refactorUnits.add(unit);
    }

    /**
     * central commit handler to perform all refactorings on the
     * source base this context targets
     *
     * @throws IOException
     */
    @Override
    public void commit() throws IOException {

        refactorUnits = refactorUnits.stream()
                .sorted(Comparator.comparing(a -> Integer.valueOf(a.getStartOffset())))
                .collect(Collectors.toList());

        super.refactorContent(refactorUnits);
        super.commit();
        refactorUnits = Lists.newLinkedList();
    }

    public void reformat() {
        smartInvokeLater(project, () -> {
            writeTransaction(project, () -> {
                CodeStyleManager.getInstance(project).reformat(psiFile);
            });
        });


    }

}
