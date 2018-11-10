package net.werpu.tools.actions_all.shared;

import static net.werpu.tools.supportive.utils.IntellijUtils.getTsExtension;

public class SimpleFileNameTransformer implements FileNameTransformer {
    @Override
    public String transform(String className) {
        return className + getTsExtension();
    }
}
