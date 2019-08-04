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

package net.werpu.tools.supportive.transformations.i18n;

import lombok.Data;
import net.werpu.tools.supportive.fs.common.I18NFileContext;
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
public class I18NEntry {


    I18NFileContext rootFile;
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
    Optional<Map<String, I18NEntry>> subValues;

    /**
     * the original element hosting the entry
     * (including the quotes etc...)
     */
    PsiElementContext psiElementContext;



    /*
     * simplified accessors into the subtree
     */
    public void addSubValue(I18NEntry entry) {
        if(!subValues.isPresent()) {
            subValues = Optional.ofNullable(new TreeMap<>());
        }

        subValues.get().put(entry.getKey().getText(), entry);
    }

    public Optional<I18NEntry> getSubValue(String key) {
        if(!subValues.isPresent()) {
            return Optional.empty();
        }
        if(!subValues.get().containsKey(key)) {
            return Optional.empty();
        }
        return Optional.of(subValues.get().get(key));
    }
}
