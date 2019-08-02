package net.werpu.tools.supportive.fs.common;

import com.google.common.base.Strings;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import lombok.Getter;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.*;


/**
 * context for holding a L18n entry for the tree
 * <p>
 * This can be used as a base for transformations
 * and displaying the the L18N Trees
 * <p>
 * The idea is to have a context which holds both, the psi reference
 * and the tree reference for the transformation
 */
@Getter
public class PsiL18nEntryContext extends PsiElementContext implements IAngularFileContext {

    public static final String ROOT_KEY = "_root_";
    /**
     * psi reference to the file
     */
    PsiElementContext rootPsiReference;


    /**
     * tree reference for display and transformation
     */
    L18NElement rootTreeReference;

    PsiElementContext exportVar; //only for ts maps


    public PsiL18nEntryContext(PsiElementContext rootPsiReference) {
        super(rootPsiReference.element);
        parseRootTreeReference(rootPsiReference);
        this.parse();
    }

    private void parseRootTreeReference(PsiElementContext rootPsiReference) {
        if (isTS()) {
            this.rootPsiReference = rootPsiReference.$q(TYPE_SCRIPT_VARIABLE, JS_OBJECT_LITERAL_EXPRESSION).findFirst().get();
            this.exportVar = this.rootPsiReference.$q(PARENTS_EQ(TYPE_SCRIPT_VARIABLE)).findFirst().get();
        } else {
            this.rootPsiReference = rootPsiReference.$q(ANY(JSON_OBJECT, JS_OBJECT_LITERAL_EXPRESSION)).findFirst().get();
        }
    }


    /**
     * parses the incoming element into the tree we all know
     */
    private void parse() {
        //step 1 typescript or json parse
        rootTreeReference = new L18NElement(null, ROOT_KEY, null);
        if (isTS()) {
            rootTreeReference.getSubElements().addAll(parseTypescriptLine(rootTreeReference, rootPsiReference));
        } else { //json‚
            rootTreeReference.getSubElements().addAll(parseJsonLine(rootTreeReference, rootPsiReference));
        }
    }

    private boolean isTS() {
        return IntellijUtils.isTypescript(getElement().getLanguage().getAssociatedFileType());
    }

    public List<L18NElement> parseTypescriptLine(L18NElement parent, PsiElementContext par) {
        List<L18NElement> childs = par.$q(CHILD_ELEM, JS_PROPERTY).map(foundElem -> {
            Optional<PsiElementContext> key = foundElem.$q(PSI_ELEMENT_JS_IDENTIFIER).findFirst();
            Optional<PsiElementContext> value = foundElem.$q(ALL(
                    BL(
                            DIRECT_CHILD(JS_LITERAL_EXPRESSION), DIRECT_CHILD(PSI_ELEMENT_JS_STRING_LITERAL)
                    ),
                    DIRECT_CHILD(JS_OBJECT_LITERAL_EXPRESSION),
                    DIRECT_CHILD(JS_STRING_TEMPLATE_EXPRESSION)
            )).reduce((el1, el2) -> el2);
            //end or recursion reached‚
            if (!key.isPresent() || !value.isPresent()) {
                return null;
            }
            //check if value is a string then return the element
            //fif not parse further down
            return getL18NElement(parent, key, value);
        }).filter(el -> el != null).collect(Collectors.toList());
        return childs;
    }

    @NotNull
    private L18NElement getL18NElement(L18NElement parent, Optional<PsiElementContext> key, Optional<PsiElementContext> value) {
        String valueType = value.get().getElement().toString();
        if (valueType.startsWith(JSON_STRING_LITERAL) || valueType.startsWith(PSI_ELEMENT_JS_STRING_LITERAL)  || valueType.startsWith(JS_STRING_TEMPLATE_EXPRESSION)) {
            return new L18NElement(parent, key.get().getUnquotedText(), value.get().getUnquotedText());
        } else {
            L18NElement entry = new L18NElement(parent, key.get().getUnquotedText(), null);
            if(isTS()) {
                entry.getSubElements().addAll(parseTypescriptLine(entry, value.get()));
            } else {
                entry.getSubElements().addAll(parseJsonLine(entry, value.get()));
            }

            return entry;
        }
    }


    /**
     * parse a json entry like wiuth key value
     *
     * @param parent
     * @param par
     * @return
     */
    public List<L18NElement> parseJsonLine(L18NElement parent, PsiElementContext par) {
        List<L18NElement> childs = par.$q(CHILD_ELEM, JSON_PROPERTY).map(foundElem -> {
            Optional<PsiElementContext> key = foundElem.$q(CHILD_ELEM, JSON_STRING_LITERAL).findFirst();
            Optional<PsiElementContext> value = foundElem.$q(ALL(DIRECT_CHILD(JSON_STRING_LITERAL), DIRECT_CHILD(JSON_OBJECT)))
                    .reduce((el1, el2) -> el2);
            //end or recursion reached‚
            if (!key.isPresent() || !value.isPresent()) {
                return null;
            }
            //check if value is a string then return the element
            //fif not parse further down
            return getL18NElement(parent, key, value);
        }).filter(el -> el != null).collect(Collectors.toList());
        return childs;
    }

    @Override
    public String getDisplayName() {
        return rootPsiReference.getName();
    }

    @Override
    public String getResourceName() {
        String key = rootTreeReference.getKey();
        return (!Strings.isNullOrEmpty(key)) ? key : rootPsiReference.getElement().getContainingFile().getName();
    }

    @Override
    public NgModuleFileContext getParentModule() {
        throw new RuntimeException("Not supported in this class");
    }

    @Override
    public VirtualFile getVirtualFile() {
        return rootPsiReference.getElement().getContainingFile().getVirtualFile();
    }

    @Override
    public PsiFile getPsiFile() {
        return rootPsiReference.getElement().getContainingFile();
    }

    @Override
    public Icon getIcon() {
        return null;
    }
}
