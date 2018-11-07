import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import util.TestUtils;

public abstract class BaseTsTest extends LightCodeInsightFixtureTestCase {
    PsiFile fs;Project prj;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.allowTreeAccessForAllFiles();
        fs = myFixture.configureByFile("Routes.ts");
        prj = myFixture.getProject();
    }



    protected String getTestDataPath() {
        return TestUtils.JS_TEST_PROBES_PATH;
    }

    @Override
    protected String getBasePath() {
        return TestUtils.JS_TEST_PROBES_PATH;
    }

    //nothing else seems to work than the hard approach
    protected boolean assertTestable() {
        return TestUtils.isTsTestable();
    }


}
