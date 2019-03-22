package net.werpu.tools.supportive.fs.common;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.yourkit.util.Strings;
import lombok.Getter;
import net.werpu.tools.supportive.utils.IntellijUtils;

import javax.swing.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.*;


/**
 * context for holding a L18n entry for the tree
 *
 * This can be used as a base for transformations
 * and displaying the the L18N Trees
 *
 * The idea is to have a context which holds both, the psi reference
 * and the tree reference for the transformation
 *
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


    public PsiL18nEntryContext(PsiElementContext rootPsiReference) {
        super(rootPsiReference.element);
        this.rootPsiReference = rootPsiReference.$q(ANY(JSON_OBJECT, JS_OBJECT_LITERAL_EXPRESSION)).findFirst().get();
        this.parse();
    }




    /**
     * parses the incoming element into the tree we all know
     */
    private void parse() {
        //step 1 typescript or json parse
        rootTreeReference = new L18NElement(null, ROOT_KEY, null);
        if(IntellijUtils.isTypescript(getPsiFile().getVirtualFile().getFileType())) {
             rootTreeReference.getSubElements().addAll(parseTypescriptLine(rootTreeReference, rootPsiReference));
        } else { //json‚
             rootTreeReference.getSubElements().addAll(parseJsonLine(rootTreeReference, rootPsiReference));
        }
    }

    public List<L18NElement> parseTypescriptLine(L18NElement parent, PsiElementContext par) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public List<L18NElement> parseJsonLine(L18NElement parent, PsiElementContext par) {
        List<L18NElement> childs = par.$q(CHILD_ELEM, JSON_PROPERTY).map(foundElem -> {
            Optional<PsiElementContext> key = foundElem.$q(CHILD_ELEM, JSON_STRING_LITERAL).findFirst();
            Optional<PsiElementContext>  value = foundElem.$q(ALL(DIRECT_CHILD(JSON_STRING_LITERAL), DIRECT_CHILD(JSON_OBJECT)))
                    .reduce((el1, el2) -> el2);
            //end or recursion reached‚
            if(!key.isPresent() || !value.isPresent()) {
                return null;
            }
            //check if value is a string then return the element
            //fif not parse further down
            if(value.get().getElement().toString().startsWith(JSON_STRING_LITERAL)) {
                return new L18NElement(parent, key.get().getUnquotedText(), value.get().getUnquotedText());
            } else {
                L18NElement entry = new L18NElement(parent, key.get().getUnquotedText(), null);
                entry.getSubElements().addAll(parseJsonLine(entry, value.get()));
                return entry;
            }
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
