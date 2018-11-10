package net.werpu.tools.actions_all.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import net.werpu.tools.supportive.fs.common.ComponentFileContext;

import java.util.Arrays;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ComponentSelectorModel {

    @NotNull
    private final ComponentFileContext[] componentFileContexts;
    private int selectedIndex = 0;

    public String[] getContextNames() {
        return Arrays.stream(componentFileContexts)
                .map(context -> context.getDisplayName())
                .toArray(size -> new String[size]);
    }
}
