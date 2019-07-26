package net.werpu.tools.gui.support;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.L18NFileContext;
import net.werpu.tools.supportive.utils.FileEndings;

import java.util.Optional;

@Getter
@EqualsAndHashCode
public class IntelliFileContextComboboxModelEntry {
    @EqualsAndHashCode.Exclude
    L18NFileContext value; //json or typescript
    L18NFileContext alternative;


    @EqualsAndHashCode.Exclude
    String label;


    String popup;

    public IntelliFileContextComboboxModelEntry(@NonNull IntellijFileContext value) {
        this(value, null);
    }

    public IntelliFileContextComboboxModelEntry(@NonNull IntellijFileContext value, IntellijFileContext alternative) {

        this.value = new L18NFileContext(value);
        this.alternative = (alternative != null) ? new L18NFileContext(alternative) : null;


        String rawName = value.getBaseName();
        String fileEnding = value.getFileEnding();
        this.label = rawName + " ( "+ fileEnding;
        if(alternative != null) {
            fileEnding = alternative.getFileEnding();
            this.label +=  " / "+fileEnding;
        }
        this.label += " ) ";

        this.popup = value.getVirtualFile().getPath()+this.label;
    }


    @Override
    public String toString() {
        return getLabel();
    }

    public Optional<L18NFileContext> getJSONFile() {
       if(value != null && value.getFileEnding().equals(FileEndings.JSON)) {
           return Optional.ofNullable(value);
       }
        if(alternative != null && alternative.getFileEnding().equals(FileEndings.JSON)) {
            return Optional.ofNullable(alternative);
        }
        return Optional.empty();
    }

    public Optional<L18NFileContext> getTsFile() {
        if(alternative != null && alternative.getFileEnding().equals(FileEndings.TS)) {
            return Optional.ofNullable(alternative);
        }
        if(value != null && value.getFileEnding().equals(FileEndings.TS)) {
            return Optional.ofNullable(value);
        }
        return Optional.empty();
    }
}
