import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import net.werpu.tools.supportive.transformations.AngularJSModuleTransformationModel;
import util.TestUtils;

/**
 * Testcases to test the various aspects of the
 * angularjs -> tn -> angular transformations
 *
 * This should help to stabilize the parsing algorithms
 * before adding a ui (the ui will be the last part which will be added
 *
 */
public class AngularTransformationsTest extends LightCodeInsightFixtureTestCase {



    @Override
    protected String getTestDataPath() {
        return TestUtils.JS_TEST_PROBES_PATH;
    }



    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.allowTreeAccessForAllFiles();


        //fs = myFixture.configureByFiles("pureAngularJS/probeComponent.ts", "pureAngularJS/probeModule.ts", "pureAngularJS/probeService.ts");
        //prj = myFixture.getProject();
    }

    /**
     * test a basic module analysis and if the subparts are properly detected
     */
    public void testBasickModuleAnalysis() {
        PsiFile psiFile = myFixture.configureByFile("pureAngularJS/probeModule.ts");
        Project project = myFixture.getProject();

        AngularJSModuleTransformationModel ctx = new AngularJSModuleTransformationModel(project, psiFile);

        assertTrue(ctx.getModuleName().equals("app.entry"));
        assertTrue(ctx.getLastImport().isPresent());
        assertTrue(ctx.getModuleDeclStart().isPresent());
        assertTrue(ctx.getRequires().contains("bla"));
        assertTrue(ctx.getRequires().contains("bla1"));
        assertTrue(ctx.getRequires().size() == 2);
        assertTrue(ctx.getModuleDefStart().isPresent() &&
                ctx.getModuleDefStart().get().getTextOffset() == ctx.getModuleDeclStart().get().getTextOffset() );

        assertTrue(ctx.getLegacyName().equals("entry"));
        assertTrue(ctx.getLegacyName().equals("entry"));
        assertTrue(ctx.getModuleClassName().equals("AppEntry"));
        assertTrue(ctx.getDeclarationsPart().startsWith(".component("));

    }

}
