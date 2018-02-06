import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class JSExistsRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
        JSInSystem annotation = description.getAnnotation(JSInSystem.class);
        if (annotation == null) {
            return statement;
        }
        String testDataSet = System.getProperty("dataset");

        try {
            Class.forName("com.intellij.lang.typescript.psi.TypeScriptExternalModuleReference");
            return statement;
        } catch (ClassNotFoundException e) {
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    // Return an empty Statement object for those tests
                    // that shouldn't run on the specified dataset.
                }
            };
        }


    }
}
