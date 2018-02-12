package util;

public class TestUtils {
    public static final String JS_TEST_PROBES_PATH = "src/test/typescript/probes";
    public static final String JAVA_TEST_PROBES_PATH = "src/test/java/probes";

    public static boolean isTsTestable() {
        try {
            Class.forName("com.intellij.lang.typescript.psi.TypeScriptExternalModuleReference");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
