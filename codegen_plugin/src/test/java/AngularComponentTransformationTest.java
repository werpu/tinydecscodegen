/*
 *
 *
 * Copyright 2020 Werner Punz
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
 * /
 */

import com.google.common.base.Strings;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.tinydecs.TinyDecsComponentTransformationModel;
import net.werpu.tools.supportive.transformations.tinydecs.TnAngularComponentTransformation;
import util.TestUtils;

import java.io.IOException;

public class AngularComponentTransformationTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return TestUtils.JS_TEST_PROBES_PATH;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.allowTreeAccessForAllFiles();
    }

    /**
     * it must perform a basic component parsing properly
     */
    public void testBasicComponentAnalysis() {
        PsiFile psiFile = myFixture.configureByFile("tinydecs/probeComponent.ts");
        Project project = myFixture.getProject();

        TinyDecsComponentTransformationModel ctx = new TinyDecsComponentTransformationModel(new IntellijFileContext(project, psiFile));

        assertTrue("no error throw in analysis", true);
    }

    public void testAnalysisStructure() {
        PsiFile psiFile = myFixture.configureByFile("tinydecs/probeComponent.ts");
        Project project = myFixture.getProject();

        TinyDecsComponentTransformationModel ctx = new TinyDecsComponentTransformationModel(new IntellijFileContext(project, psiFile));

        assertTrue("tag name must be parsed", "probe-component".equals(ctx.getSelectorName()));
        assertTrue("controllerAs must be present", "ctrl".equals(ctx.getControllerAs()));
        assertTrue("injects must be parsed", ctx.getInjects().size() == 2);

        assertTrue("injects must be parsed", ctx.getInjects().get(0).getName().equals("'$scope'"));
        assertTrue("injects must be parsed", ctx.getInjects().get(0).getType().equals("IScope"));
        assertTrue("injects must be parsed", ctx.getInjects().get(0).getTsNameType().equals("$scope"));


        assertTrue("injects must be parsed", ctx.getInjects().get(1).getName().equals("GlobalSearchOptions"));
        assertTrue("injects must be parsed", ctx.getInjects().get(1).getType().equals("GlobalSearchOptions"));
        assertTrue("injects must be parsed", ctx.getInjects().get(1).getTsNameType().equals("opts"));


        assertTrue("classblock must be preset", ctx.getClassBlock() != null);
        assertTrue("rootblock must be preset", ctx.getRootBlock() != null);
        assertTrue("watches must be present", ctx.getWatches().size() == 2);
        assertTrue("watches must be present", ctx.getWatches().get(1).getOldValueVarName() == null);
        assertTrue("watches must be present", ctx.getWatches().get(1).getNewValueVarName().equals("newValue"));
        assertTrue("watches must be present", ctx.getWatches().get(1).getNewValueVarType().equals("string[]"));
        assertTrue("constructor must be referenced", ctx.getConstructorDef().isPresent());
        assertTrue("postlink present", ctx.getPostLinkDef().isPresent());
        assertTrue("onInit must be present", ctx.getOnInitDef().isPresent());
        assertTrue("onDestroy must be present", !ctx.getDestroyDef().isEmpty());
        assertTrue("classname must be correct", ctx.getClazzName().equals("ProbeComponent"));
        assertTrue("two attributes", ctx.getPossibleClassAttributes().size() == 2);
        assertTrue("bindings must be set", ctx.getBindings().size() == 5);


        assertTrue("other methods must be passed through 1:1", ctx.getPassThroughMethods().size() == 1);
        // assertTrue("both searchoptions", ctx.get);
    }

    public void testTransformationResult() throws IOException {
        PsiFile psiFile = myFixture.configureByFile("tinydecs/probeComponent.ts");
        Project project = myFixture.getProject();

        TinyDecsComponentTransformationModel ctx = new TinyDecsComponentTransformationModel(new IntellijFileContext(project, psiFile));
        TnAngularComponentTransformation transformation = new TnAngularComponentTransformation(ctx);

        String result = transformation.getNgTransformation();
        assertTrue("result must not be empty", !Strings.isNullOrEmpty(result));

        assertTrue("imports must be processed", result.indexOf("${data.imports}") == -1);
        assertTrue("additional imports must be processed", result.indexOf("${data.fromImportsToClassDecl}") == -1);
        assertTrue("class declaration must be there", result.indexOf("export class ProbeComponent") != -1);
        assertTrue("class declaration must be there", result.indexOf("export class ProbeComponent") != -1);
        assertTrue("component declaration must be there", result.indexOf("@Component({") != -1);
        assertTrue("selector must be there", result.indexOf("selector: \"probe-component\"") != -1);

        //TODO attributes, injectors, methods, bindings etc...




    }



}