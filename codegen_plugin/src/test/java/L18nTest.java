import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import net.werpu.tools.supportive.fs.common.ComponentFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.L18NFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.transformations.L18NTransformationModel;
import net.werpu.tools.supportive.transformations.modelHelpers.PARSING_TYPE;
import net.werpu.tools.supportive.utils.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

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


    /**
     * Testbed for the l18n parsing
     */
    @Ignore
    @Test
    public void testL18NFileParsing() {
        PsiFile i18nFile = myFixture.configureByFiles("i18n/labels-en.json", "angular_js/angular.js")[0];

        L18NFileContext fc = new L18NFileContext(i18nFile.getProject(), i18nFile);

        String key = fc.getKey("testString").get(0);
        assertTrue(key.equals("test"));

        key = fc.getKey("testString_xx").get(0);
        assertTrue(key.equals("test3.test"));

        Optional<PsiElementContext> value = fc.getValue("test3","test");
        assertTrue(value.isPresent());
        assertTrue(StringUtils.stripQuotes(value.get().getText()).equals("testString_xx"));

        Optional<String> valueStr = fc.getValueAsStr("test3","test");
        assertTrue(valueStr.isPresent());
        assertTrue(valueStr.get().equals("testString_xx"));


    }
}
