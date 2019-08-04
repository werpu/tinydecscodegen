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
import net.werpu.tools.supportive.fs.common.ComponentFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import org.junit.Test;

import static util.TestUtils.JS_TEST_PROBES_PATH;

public class ComponentTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    protected String getTestDataPath() {
        return JS_TEST_PROBES_PATH;
    }


    @Test
    public void testController() {
        PsiFile componentFile = myFixture.configureByFile("module1/View1.ts");
        ComponentFileContext ctx = new ComponentFileContext(new IntellijFileContext(componentFile.getProject(), componentFile));
        assertTrue(ctx.getTagName().equals("view1"));
        assertTrue(ctx.getDisplayName().equals("View1"));
        //assertTrue(ctx.get().equals("View1"));
        assertTrue(ctx.getTemplateText().isPresent());

    }

    @Test
    public void testController2() {
        myFixture.configureByFile("pages/main-page.component.html");
        PsiFile componentFile = myFixture.configureByFile("pages/main-page.component.ts");
        ComponentFileContext ctx = new ComponentFileContext(new IntellijFileContext(componentFile.getProject(), componentFile));
        assertTrue(ctx.getTagName().equals("app-main-page"));
        assertTrue(ctx.getClazzName().equals("MainPageComponent"));
        //assertTrue(ctx.get().equals("View1"));
        assertTrue(ctx.getTemplateRef().isPresent());

    }



}
