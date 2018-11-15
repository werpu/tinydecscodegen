import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.AngularJSComponentTransformationModel;
import net.werpu.tools.supportive.transformations.AngularJSModuleTransformationModel;
import net.werpu.tools.supportive.transformations.modelHelpers.BindingType;
import org.junit.Ignore;
import util.TestUtils;

import static net.werpu.tools.supportive.transformations.modelHelpers.BindingType.*;

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
    public void testBasicModuleAnalysis() {
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


    public void testBasicComponentAnalysis() {
        PsiFile module =  myFixture.configureByFile("pureAngularJS/probeModule.ts");
        PsiFile psiFile = myFixture.configureByFile("pureAngularJS/probeComponent.ts");
        Project project = myFixture.getProject();


        AngularJSComponentTransformationModel ctx = new AngularJSComponentTransformationModel(new IntellijFileContext(project, psiFile));

        assertTrue(ctx.getClazzName().equals("ProbeComponent"));
        assertTrue(ctx.getBindings().size() == 5);
        assertTrue(ctx.getBindings().get(0).getName().equals("searchOptions"));
        assertTrue(ctx.getBindings().get(3).getName().equals("navigate"));
        assertTrue(ctx.getBindings().get(0).getBindingType() == BOTH);
        assertTrue(ctx.getBindings().get(3).getBindingType() == FUNC);


        assertTrue(ctx.getInjects().size() == 1);


        assertTrue(ctx.getInjects().get(0).getName().equals("$scope"));


        //3 inline functions only one of those should be externalizable
        //by contract
        //a function can only be externalizable
        //if it does not have any references into its outer shell
        //we cannot simply remap parameters, because those functions
        //might be used within the template, where
        //we do not have parameter control at all

        assertTrue(ctx.getInlineFunctions().size() == 3);
        assertTrue(!ctx.getInlineFunctions().get(0).isExternalizale());
        assertTrue(!ctx.getInlineFunctions().get(1).isExternalizale());
        //TODO not yet implemented
        assertTrue(ctx.getInlineFunctions().get(2).isExternalizale());
        assertTrue(ctx.getSelectorName().equals("probe-component"));




    }

}
