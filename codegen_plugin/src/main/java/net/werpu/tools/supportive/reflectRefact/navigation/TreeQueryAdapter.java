package net.werpu.tools.supportive.reflectRefact.navigation;

import java.util.List;
import java.util.function.Function;

public interface TreeQueryAdapter<T> {
    List<T> parents(T ctx);

    List<T> findChildren(T ctx, Function<T, Boolean> visitor);

    List<T> findElements(T ctx, Function<T, Boolean> visitor);

    List<T> walkParents(T ctx, Function<T, Boolean> visitor);

    String getText(T ctx);

    String getName(T ctx);

    String toString(T ctx);
}
