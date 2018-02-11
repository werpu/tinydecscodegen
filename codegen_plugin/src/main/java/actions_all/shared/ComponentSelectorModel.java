package actions_all.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import supportive.fs.common.ComponentFileContext;

import java.util.Arrays;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ComponentSelectorModel {

    private int selectedIndex = 0;

    @NotNull
    private final ComponentFileContext[] componentFileContexts;


    public String[] getContextNames() {
        return Arrays.stream(componentFileContexts)
                .map(context -> context.getDisplayName())
                .toArray(size -> new String[size]);
    }
}
