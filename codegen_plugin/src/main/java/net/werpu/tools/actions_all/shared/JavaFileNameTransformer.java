package net.werpu.tools.actions_all.shared;

public class JavaFileNameTransformer implements FileNameTransformer {
    @Override
    public String transform(String className) {
        return className + ".java";
    }
}
