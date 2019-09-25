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

package net.werpu.tools.gui.support;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.werpu.tools.supportive.fs.common.I18NFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.FileEndings;

import java.util.Optional;

@Getter
@EqualsAndHashCode
public class IntelliFileContextComboboxModelEntry {
    @EqualsAndHashCode.Exclude
    I18NFileContext value; //json or typescript
    I18NFileContext alternative;
    @EqualsAndHashCode.Exclude
    String label;
    String popup;

    public IntelliFileContextComboboxModelEntry(@NonNull IntellijFileContext value) {
        this(value, null);
    }

    public IntelliFileContextComboboxModelEntry(@NonNull IntellijFileContext value, IntellijFileContext alternative) {

        this.value = new I18NFileContext(value);
        this.alternative = (alternative != null) ? new I18NFileContext(alternative) : null;

        String rawName = value.getBaseName();
        String fileEnding = value.getFileEnding();
        this.label = rawName + " ( " + fileEnding;
        if (alternative != null) {
            fileEnding = alternative.getFileEnding();
            this.label += " / " + fileEnding;
        }
        this.label += " ) ";

        this.popup = value.getVirtualFile().getPath() + this.label;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    public Optional<I18NFileContext> getJSONFile() {
        if (value != null && value.getFileEnding().equals(FileEndings.JSON)) {
            return Optional.ofNullable(value);
        }
        if (alternative != null && alternative.getFileEnding().equals(FileEndings.JSON)) {
            return Optional.ofNullable(alternative);
        }
        return Optional.empty();
    }

    public Optional<I18NFileContext> getTsFile() {
        if (alternative != null && alternative.getFileEnding().equals(FileEndings.TS)) {
            return Optional.ofNullable(alternative);
        }
        if (value != null && value.getFileEnding().equals(FileEndings.TS)) {
            return Optional.ofNullable(value);
        }
        return Optional.empty();
    }
}
