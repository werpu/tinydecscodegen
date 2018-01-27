package actions.shared;

public class SimpleFileNameTransformer implements FileNameTransformer {
    @Override
    public String transform(String className) {
        return className+".ts";
    }
}
