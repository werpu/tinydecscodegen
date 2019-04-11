package net.werpu.tools.gui.support;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.L18NFileContext;

@Getter
@EqualsAndHashCode
public class IntelliFileContextComboboxModelEntry {
    @EqualsAndHashCode.Exclude
    L18NFileContext value;


    @EqualsAndHashCode.Exclude
    String label;


    String popup;


    public IntelliFileContextComboboxModelEntry(@NonNull IntellijFileContext value) {
        this.value = new L18NFileContext(value);

        this.label = value.getVirtualFile().getName();
        this.popup = value.getVirtualFile().getPath()+this.label;
    }



    @Override
    public String toString() {
        return getLabel();
    }


}