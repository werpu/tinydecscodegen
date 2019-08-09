package net.werpu.tools.toolWindows.supportive;

import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import net.werpu.tools.supportive.fs.common.I18NFileContext;
import net.werpu.tools.supportive.fs.common.L18NElement;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.PsiI18nEntryContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.*;
import static java.util.Collections.*;

/**
 * Node for the i18n toolwindow, so that we easily can change
 * etc...
 */
@Setter
@Getter
public class I18NToolWindowModel {

    List<PsiI18nEntryContext> entryContext;

    //context root tree context pointing to the root element
    Map<String, List<I18NFileContext>> fileIdx = new HashMap<>();

    /**
     * merged tree from all entry contexts
     */
    List<L18NElement> mergedTree;

    private Project project;

    public I18NToolWindowModel(I18NFileContext... i18nFiles) {
        entryContext = stream(i18nFiles)
                .map(fileContext -> new PsiElementContext(fileContext.getPsiFile()))
                .map(psiElementContext -> new PsiI18nEntryContext(psiElementContext))
                .collect(Collectors.toList());

        project = i18nFiles[0].getProject();

        //We now merge both trees on order, last one overwrites preexisting ones for the time being
        //just as angular does
        mergedTree = entryContext.stream().map(el -> el.getRootTreeReference()).collect(Collectors.toList());
        if(mergedTree.size() > 1) {
            mergedTree = mergedTree.stream().map(el -> singletonList(el)).reduce(new ArrayList<>(), (el1, el2) -> mergeLists(el1, el2));
        }

        rebuildIdx();
    }

    /**
     * build up a file idx so that each key can get a list of appropriate files (typescript javascript and in the future maybe others)
     */
    public void rebuildIdx() {
        fileIdx = new HashMap<>();
        entryContext.stream().forEach(psiI18nEntryContext -> updateKeyIndex("", psiI18nEntryContext));
    }


    private void updateKeyIndex(String treePrefix, PsiI18nEntryContext element) {
        L18NElement elementReference = element.getRootTreeReference();
        updateFileIndex(treePrefix, new I18NFileContext(project, element.getPsiFile()), elementReference);
    }

    private void updateFileIndex(String treePrefix, I18NFileContext i18nFileRef, L18NElement elementReference) {
        final String finalKey = treePrefix + elementReference.getKey();


        if(!fileIdx.containsKey(finalKey)) {
            fileIdx.put(finalKey, new ArrayList<>());
        }

        fileIdx.get(finalKey).add(i18nFileRef);
        elementReference.getSubElements().stream().forEach(l18NElement -> updateFileIndex(finalKey, i18nFileRef, l18NElement));
    }

    /**
     * The idea is to perform a tree merge on a single level with last one
     * being the important one
     *
     * @param list1
     * @param list1
     * @return
             */
    private List<L18NElement> mergeLists(List<L18NElement> list1, List<L18NElement> list2) {
        final Map<String, L18NElement> list1Idx = new HashMap<>();
        final Map<String, L18NElement> list12dx = new HashMap<>();
        list1.stream().forEach(el -> list1Idx.put(el.getKey(), el));
        list2.stream().forEach(el -> list12dx.put(el.getKey(), el));

        List<L18NElement[]> groupsList = Stream.concat(
                list1.stream().map(el -> {
                    if (!list12dx.containsKey(el.getKey())) {
                        return new L18NElement[]{el, null};
                    } else {
                        return new L18NElement[]{el, list12dx.get(el.getKey())};
                    }
                }),
                list2.stream().map(el -> {
                    if (!list1Idx.containsKey(el.getKey())) {
                        return new L18NElement[]{null, el};
                    }
                    return null;
                }).filter(el -> el != null)
        ).collect(Collectors.toList());
        return groupsList.stream().map(el -> {

            if (el[0] == null) {
                return el[1];
            }
            if (el[1] == null) {
                return el[0];
            } else {
                List<L18NElement> mergedList = mergeLists(el[0].getSubElements(), el[1].getSubElements());
                L18NElement retVal = new L18NElement(el[1].getParent(), el[1].getKey(), el[1].getStringValue());
                retVal.getSubElements().addAll(mergedList);
                return retVal;
            }
        }).collect(Collectors.toList());
    }

}
