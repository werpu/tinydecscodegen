package actions_all.shared;

import static supportive.utils.IntellijUtils.getTsExtension;

public class SimpleFileNameTransformer implements FileNameTransformer {
    @Override
    public String transform(String className) {
        return className + getTsExtension();
    }
}
