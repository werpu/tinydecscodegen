import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.PsiL18nEntryContext;
import org.junit.Test;

import static util.TestUtils.JS_TEST_PROBES_PATH;

/**
 * Test for the json/typescript -> L18NTree parsing and vice versa
 */
public class L18NTreeTest extends LightCodeInsightFixtureTestCase {


    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected String getTestDataPath() {
        return JS_TEST_PROBES_PATH;
    }


    @Test
    public void testExprTranslate() {
        PsiFile resourceFile = myFixture.configureByFiles("i18n/labels-en.json", "angular_js/angular.js")[0];
        PsiL18nEntryContext treeContext = new PsiL18nEntryContext(new PsiElementContext(resourceFile));


        assertTrue(treeContext.getRootTreeReference() != null);
        assertTrue(treeContext.getRootTreeReference().getKey().equals(PsiL18nEntryContext.ROOT_KEY));
        assertTrue(treeContext.getRootTreeReference().getStringValue() == null);
        assertTrue(!treeContext.getRootTreeReference().getSubElements().isEmpty());
        assertTrue("all keys are determined", treeContext.getRootTreeReference().getSubElements().size() == 3);
        assertTrue("last element has subelements", !treeContext.getRootTreeReference().getSubElements().get(2).getSubElements().isEmpty());
        assertTrue("last element has subelements of subElements4", treeContext.getRootTreeReference().getSubElements().get(2).getSubElements().get(1).getSubElements().size() == 2);
    }

    @Test
    public void testSameTestForTypescript() {
        PsiFile resourceFile = myFixture.configureByFiles("i18n/labels-en.ts", "angular_js/angular.js")[0];
        PsiL18nEntryContext treeContext = new PsiL18nEntryContext(new PsiElementContext(resourceFile));


        assertTrue(treeContext.getRootTreeReference() != null);
        assertTrue(treeContext.getRootTreeReference().getKey().equals(PsiL18nEntryContext.ROOT_KEY));
        assertTrue(treeContext.getRootTreeReference().getStringValue() == null);
        assertTrue(!treeContext.getRootTreeReference().getSubElements().isEmpty());
        assertTrue("all keys are determineL18NTreeTestd", treeContext.getRootTreeReference().getSubElements().size() == 4);
        assertTrue("last element has subelements", !treeContext.getRootTreeReference().getSubElements().get(2).getSubElements().isEmpty());
        assertTrue("last element has subelements of subElements4", treeContext.getRootTreeReference().getSubElements().get(2).getSubElements().get(1).getSubElements().size() == 2);


        String varName = treeContext.getExportVar().getName();
        assertTrue("Varname properly defined", varName.equals("language"));
    }

}
