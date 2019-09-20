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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Model representation of an L18N Tree
 * it either is a key->string value pair or a key -> L18nSubtree
 * key value pair
 */

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class I18NElement {

    @EqualsAndHashCode.Exclude
    private final I18NElement parent;

    private final String key;

    @EqualsAndHashCode.Exclude
    private final String stringValue;

    @EqualsAndHashCode.Exclude
    @NotNull
    List<I18NElement> subElements = new ArrayList<>();

    public String getFullKey() {
        List<String> keys = new ArrayList<>();
        keys.add(key);
        I18NElement par = parent;
        while(par != null) {
            String parKey = Strings.nullToEmpty(par.getKey());
            if(parKey != null && !parKey.equals("_root_")) { //root element no key there
                keys.add(parKey);
            }
            par = par.getParent();
        }
        keys = Lists.reverse(keys);
        return keys.stream()
                .collect( Collectors.joining( "." ));
    }

}
