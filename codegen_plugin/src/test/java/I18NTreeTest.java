/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.PsiI18nEntryContext;
import org.junit.Test;

import static util.TestUtils.JS_TEST_PROBES_PATH;

/**
 * Test for the json/typescript -> L18NTree parsing and vice versa
 */
public class I18NTreeTest extends LightCodeInsightFixtureTestCase {


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
        PsiI18nEntryContext treeContext = new PsiI18nEntryContext(new PsiElementContext(resourceFile));


        assertTrue(treeContext.getRootTreeReference() != null);
        assertTrue(treeContext.getRootTreeReference().getKey().equals(PsiI18nEntryContext.ROOT_KEY));
        assertTrue(treeContext.getRootTreeReference().getStringValue() == null);
        assertTrue(!treeContext.getRootTreeReference().getSubElements().isEmpty());
        assertTrue("all keys are determined", treeContext.getRootTreeReference().getSubElements().size() == 3);
        assertTrue("last element has subelements", !treeContext.getRootTreeReference().getSubElements().get(2).getSubElements().isEmpty());
        assertTrue("last element has subelements of subElements4", treeContext.getRootTreeReference().getSubElements().get(2).getSubElements().get(1).getSubElements().size() == 2);
    }

    @Test
    public void testSameTestForTypescript() {
        PsiFile resourceFile = myFixture.configureByFiles("i18n/labels-en.ts", "angular_js/angular.js")[0];
        PsiI18nEntryContext treeContext = new PsiI18nEntryContext(new PsiElementContext(resourceFile));


        assertTrue(treeContext.getRootTreeReference() != null);
        assertTrue(treeContext.getRootTreeReference().getKey().equals(PsiI18nEntryContext.ROOT_KEY));
        assertTrue(treeContext.getRootTreeReference().getStringValue() == null);
        assertTrue(!treeContext.getRootTreeReference().getSubElements().isEmpty());
        assertTrue("all keys are determineL18NTreeTestd", treeContext.getRootTreeReference().getSubElements().size() == 4);
        assertTrue("last element has subelements", !treeContext.getRootTreeReference().getSubElements().get(2).getSubElements().isEmpty());
        assertTrue("last element has subelements of subElements4", treeContext.getRootTreeReference().getSubElements().get(2).getSubElements().get(1).getSubElements().size() == 2);


        String varName = treeContext.getExportVar().getName();
        assertTrue("Varname properly defined", varName.equals("language"));
    }

}
