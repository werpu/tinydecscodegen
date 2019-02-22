import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import net.werpu.tools.supportive.fs.common.ComponentFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.L18NTransformationModel;
import org.junit.Test;

import static util.TestUtils.JS_TEST_PROBES_PATH;

/**
 * Unit tests for the L18n subsystem,
 */
public class L18nTest  extends LightCodeInsightFixtureTestCase {

    static final int POS_STATIC_TEXT = 194;
    static final int POS_STATIC_TEXT_2 = 200;
    static final int POS_TRANSLATE = 229;
    static final int POS_ATTR = 323;
    static final int POS_EXPR = 267;
    static final int POS_EXPR_TRANSLATE = 289;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

    }


    protected String getTestDataPath() {
        return JS_TEST_PROBES_PATH;
    }


    @Test
    public void testFromComponent() {
        PsiFile componentFile = myFixture.configureByFiles("stringRefactoring/StringView.ts", "angular_js/angular.js")[0];
        ComponentFileContext ctx = new ComponentFileContext(new IntellijFileContext(componentFile.getProject(), componentFile));

        //lets try our cases on am embedded file
        L18NTransformationModel transformationModel = new L18NTransformationModel(ctx, POS_STATIC_TEXT);
        assertTrue(transformationModel.getFrom() > 0);
        assertTrue(transformationModel.getTo() > 0);

        //TODO not working yet, but language injection finally seems to work
        //assertTrue(transformationModel.getKey().equals("HELLO_FROM_VIEW1"));





    }



}
