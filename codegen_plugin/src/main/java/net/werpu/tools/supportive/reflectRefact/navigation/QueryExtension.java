package net.werpu.tools.supportive.reflectRefact.navigation;

import java.util.function.Function;
import java.util.stream.Stream;

public interface QueryExtension<T>  {
    Stream<T> apply(TreeQueryEngine<T> engine, Stream<T> value);
}
