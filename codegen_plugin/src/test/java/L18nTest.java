import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import net.werpu.tools.supportive.fs.common.ComponentFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.L18NTransformationModel;
import net.werpu.tools.supportive.transformations.modelHelpers.PARSING_TYPE;
import org.junit.Test;

import static util.TestUtils.JS_TEST_PROBES_PATH;

/**
 * Unit tests for the L18n subsystem,
 */
public class L18nTest  extends LightCodeInsightFixtureTestCase {

    static final int POS_STATIC_TEXT = 194;
    static final int POS_STATIC_TEXT_2 = 252;
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
    public void testText() {
        PsiFile componentFile = myFixture.configureByFiles("stringRefactoring/StringView.ts", "angular_js/angular.js")[0];
        ComponentFileContext ctx = new ComponentFileContext(new IntellijFileContext(componentFile.getProject(), componentFile));

        //lets try our cases on am embedded file
        L18NTransformationModel transformationModel = new L18NTransformationModel(ctx, POS_STATIC_TEXT);
        assertTrue(transformationModel.getFrom() > 0);
        assertTrue(transformationModel.getTo() > 0);

        assertTrue(transformationModel.getKey().equals("HELLO_FROM_VIEW1"));
        assertTrue(transformationModel.getParsingType() == PARSING_TYPE.TEXT);

    }

    @Test
    public void testText2() {
        PsiFile componentFile = myFixture.configureByFiles("stringRefactoring/StringView.ts", "angular_js/angular.js")[0];
        ComponentFileContext ctx = new ComponentFileContext(new IntellijFileContext(componentFile.getProject(), componentFile));

        //lets try our cases on am embedded file
        L18NTransformationModel transformationModel = new L18NTransformationModel(ctx, POS_STATIC_TEXT_2);
        assertTrue(transformationModel.getFrom() > 0);
        assertTrue(transformationModel.getTo() > 0);

        assertTrue(transformationModel.getKey().equals("CASE2"));
        assertTrue(transformationModel.getParsingType() == PARSING_TYPE.TEXT);

    }


    @Test
    public void testTranslate() {
        PsiFile componentFile = myFixture.configureByFiles("stringRefactoring/StringView.ts", "angular_js/angular.js")[0];
        ComponentFileContext ctx = new ComponentFileContext(new IntellijFileContext(componentFile.getProject(), componentFile));

        //lets try our cases on am embedded file
        L18NTransformationModel transformationModel = new L18NTransformationModel(ctx, POS_TRANSLATE);
        assertTrue(transformationModel.getFrom() > 0);
        assertTrue(transformationModel.getTo() > 0);

        assertTrue(transformationModel.getKey().equals("CASE1_FROM_ME"));
        assertTrue(transformationModel.getParsingType() == PARSING_TYPE.STRING_WITH_TRANSLATE);

    }

    @Test
    public void testAttr() {
        PsiFile componentFile = myFixture.configureByFiles("stringRefactoring/StringView.ts", "angular_js/angular.js")[0];
        ComponentFileContext ctx = new ComponentFileContext(new IntellijFileContext(componentFile.getProject(), componentFile));

        //lets try our cases on am embedded file
        L18NTransformationModel transformationModel = new L18NTransformationModel(ctx, POS_ATTR);
        assertTrue(transformationModel.getFrom() > 0);
        assertTrue(transformationModel.getTo() > 0);

        assertTrue(transformationModel.getKey().equals("ATTR2"));
        assertTrue(transformationModel.getParsingType() == PARSING_TYPE.TEXT);

    }


    @Test
    public void testExpr() {
        PsiFile componentFile = myFixture.configureByFiles("stringRefactoring/StringView.ts", "angular_js/angular.js")[0];
        ComponentFileContext ctx = new ComponentFileContext(new IntellijFileContext(componentFile.getProject(), componentFile));

        //lets try our cases on am embedded file
        L18NTransformationModel transformationModel = new L18NTransformationModel(ctx, POS_EXPR);
        assertTrue(transformationModel.getFrom() > 0);
        assertTrue(transformationModel.getTo() > 0);

        assertTrue(transformationModel.getKey().equals("CASE_3"));
        assertTrue(transformationModel.getParsingType() == PARSING_TYPE.STRING);
    }

    @Test
    public void testExprTranslate() {
        PsiFile componentFile = myFixture.configureByFiles("stringRefactoring/StringView.ts", "angular_js/angular.js")[0];
        ComponentFileContext ctx = new ComponentFileContext(new IntellijFileContext(componentFile.getProject(), componentFile));

        //lets try our cases on am embedded file
        L18NTransformationModel transformationModel = new L18NTransformationModel(ctx, POS_EXPR_TRANSLATE);
        assertTrue(transformationModel.getFrom() > 0);
        assertTrue(transformationModel.getTo() > 0);

        assertTrue(transformationModel.getKey().equals("CASE_4"));
        assertTrue(transformationModel.getParsingType() == PARSING_TYPE.STRING_WITH_TRANSLATE);
    }


}
