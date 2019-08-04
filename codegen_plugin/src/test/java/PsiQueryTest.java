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

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;
import org.junit.Test;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static util.TestUtils.JS_TEST_PROBES_PATH;

public class PsiQueryTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    protected String getTestDataPath() {
        return JS_TEST_PROBES_PATH;
    }


    //nothing else seems to work than the hard approach
    private boolean assertTestable() {
        try {
            Class.forName("com.intellij.lang.typescript.psi.TypeScriptExternalModuleReference");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    @Test
    public void testTNRouting() throws Exception {
        if (!assertTestable()) {
            return;
        }
        PsiFile psiFile = myFixture.configureByFile("TN_Routes.ts");
        Project project = myFixture.getProject();

        IntellijFileContext fileContext = new IntellijFileContext(project, psiFile);

        Stream<PsiElementContext> routeProviderQuery = fileContext.queryContent(PSI_ELEMENT_JS_IDENTIFIER, TreeQueryEngine.TEXT_EQ("$routeProvider"));
        Optional routeProvider = routeProviderQuery.findFirst();
        assertTrue(routeProvider.isPresent());
        routeProviderQuery = fileContext.queryContent(PSI_ELEMENT_JS_IDENTIFIER, TreeQueryEngine.TEXT_EQ("$routeProvider"));
        assertTrue(routeProviderQuery.collect(Collectors.toList()).size() == 4);


    }

    @Test
    public void testMethodProbe() {
        if (!assertTestable()) {
            return;
        }
        PsiFile psiFile = myFixture.configureByFile("parser/methodProbe.ts");
        Project project = myFixture.getProject();

        TypescriptFileContext ctx = new TypescriptFileContext(project, psiFile);

        assertTrue(ctx.$q(JS_REFERENCE_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER, TreeQueryEngine.TEXT_EQ("$scope")).findFirst().isPresent());
        assertTrue(ctx.$q(JS_REFERENCE_EXPRESSION, TreeQueryEngine.CHILD_ELEM,PSI_ELEMENT_JS_IDENTIFIER, TreeQueryEngine.TEXT_EQ("$scope")).findFirst().isPresent());

        assertTrue(ctx.$q(JS_REFERENCE_EXPRESSION, TreeQueryEngine.DIRECT_CHILD(PSI_ELEMENT_JS_IDENTIFIER), TreeQueryEngine.TEXT_EQ("$scope")).findFirst().isPresent());
    }
}
