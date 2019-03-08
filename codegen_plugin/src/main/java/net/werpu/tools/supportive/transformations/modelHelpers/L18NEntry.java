package net.werpu.tools.supportive.transformations.modelHelpers;

import lombok.Data;
import net.werpu.tools.supportive.fs.common.L18NFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A simplified view on our json L18n entries
 * with included psi information for easy refactoring
 *
 */
@Data
public class L18NEntry {


    L18NFileContext rootFile;
    /**
     * key, also reflected in the parents map as key
     * but for simpler access we expose it also as attribute
     */
    PsiElementContext key;
    /**
     * scalar label value
     */
    PsiElementContext value;
    /**
     * in case of a tree like structure we also have subValues
     */
    Optional<Map<String, L18NEntry>> subValues;

    /**
     * the original element hosting the entry
     * (including the quotes etc...)
     */
    PsiElementContext psiElementContext;



    /*
     * simplified accessors into the subtree
     */
    public void addSubValue(L18NEntry entry) {
        if(!subValues.isPresent()) {
            subValues = Optional.ofNullable(new TreeMap<>());
        }

        subValues.get().put(entry.getKey().getText(), entry);
    }

    public Optional<L18NEntry> getSubValue(String key) {
        if(!subValues.isPresent()) {
            return Optional.empty();
        }
        if(!subValues.get().containsKey(key)) {
            return Optional.empty();
        }
        return Optional.of(subValues.get().get(key));
    }
}
