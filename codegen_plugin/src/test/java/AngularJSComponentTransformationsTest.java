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
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.tinydecs.AngularJSComponentTransformationModel;
import net.werpu.tools.supportive.transformations.tinydecs.AngularJSDirectiveTransformationModel;
import net.werpu.tools.supportive.transformations.tinydecs.AngularJSModuleTransformationModel;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.ComponentBinding;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.Injector;
import util.TestUtils;

import static net.werpu.tools.supportive.transformations.shared.modelHelpers.BindingType.*;

/**
 * Testcases to test the various aspects of the
 * angularjs -> tn -> angular transformations
 *
 * This should help to stabilize the parsing algorithms
 * before adding a ui (the ui will be the last part which will be added
 *
 */
public class AngularJSComponentTransformationsTest extends LightJavaCodeInsightFixtureTestCase {



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
                ctx.getModuleDefStart().get().getTextRangeOffset() == ctx.getModuleDeclStart().get().getTextRangeOffset() );

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

        assertTrue(ctx.getRefactoredConstructorBlock().contains("{"));

        assertTrue(!ctx.getAttributes().isEmpty());
        assertTrue(ctx.getAttributes().size() < 3);
        assertTrue(ctx.getAttributes().stream().filter(el->el.getText().contains("boogaVar")).findFirst().isPresent());

    }


    public void testMoreDifficultComponent() {
        PsiFile module =  myFixture.configureByFile("pureAngularJS/probeModule.ts");
        PsiFile psiFile = myFixture.configureByFile("pureAngularJS/probeComponent2.ts");
        Project project = myFixture.getProject();

        AngularJSComponentTransformationModel ctx = new AngularJSComponentTransformationModel(new IntellijFileContext(project, psiFile));

        assertTrue(ctx.getClazzName().equals("SuggestionPanel"));
        assertTrue(ctx.getControllerAs().equals("ctrl2"));
        assertTrue(ctx.getRefactoredConstructorBlock().contains("var _t"));
        assertTrue(ctx.getRefactoredConstructorBlock().trim().startsWith("{"));
        assertTrue(ctx.getRefactoredConstructorBlock().trim().endsWith("}"));
    }

    /**
     * tests a parameter regression
     * where sometimes a load of weird parameters
     * are added to our function parameters.
     * The regression was found while testing probe 3
     */
    public void testParameterRegression() {
        PsiFile module =  myFixture.configureByFile("pureAngularJS/probeModule.ts");
        PsiFile psiFile = myFixture.configureByFile("pureAngularJS/probeComponent3.ts");
        Project project = myFixture.getProject();

        AngularJSComponentTransformationModel ctx = new AngularJSComponentTransformationModel(new IntellijFileContext(project, psiFile));
        assertTrue(ctx.getInjects().size() == 5);
    }

    public void testMethodRegression() {
        PsiFile module =  myFixture.configureByFile("pureAngularJS/probeModule.ts");
        PsiFile psiFile = myFixture.configureByFile("pureAngularJS/probeComponent4.ts");
        Project project = myFixture.getProject();

        AngularJSComponentTransformationModel ctx = new AngularJSComponentTransformationModel(new IntellijFileContext(project, psiFile));
        assertTrue(ctx.getInlineFunctions().size() == 9);

        ctx.getRefactoredConstructorBlock();
    }



    public void testDirective() {
        PsiFile module =  myFixture.configureByFile("pureAngularJS/probeModule.ts");
        PsiFile psiFile = myFixture.configureByFile("pureAngularJS/probeDirective.ts");
        Project project = myFixture.getProject();

        AngularJSDirectiveTransformationModel model = new AngularJSDirectiveTransformationModel(new IntellijFileContext(project, psiFile));

        assertTrue(model.getClazzName().equals("BootstrapPopup"));
        assertTrue(model.getTemplate() != null && model.getTemplate().length() > 0);
        assertTrue(model.getInjects().size() == 5);
        assertTrue(model.getInjects().contains(new Injector("$scope", "IScope")));
        assertTrue(model.getInjects().contains(new Injector("$element", "JQuery")));
        assertTrue(model.getInjects().contains(new Injector("$timeout", "ITimeoutService")));
        assertTrue(model.getInjects().contains(new Injector("$compile", "ICompileService")));
        assertTrue(model.getInjects().contains(new Injector("appUtils", "AppUtils")));

        assertTrue(model.getCodeBetweenDefinitionAndClassBlock().trim().equals("$compile.any(\"whatever\");"));


        assertTrue(model.getBindings().contains(new ComponentBinding(ASTRING, "template")));
        assertTrue(model.getBindings().contains(new ComponentBinding(ASTRING, "container")));
        assertTrue(model.getBindings().contains(new ComponentBinding(ASTRING, "selector")));
        assertTrue(model.getBindings().contains(new ComponentBinding(ASTRING, "trigger")));
       // assertTrue(model.getBindings().contains(new ComponentBinding(ASTRING, "transclude")));
        assertTrue(model.getAdditionalFunctions().size() == 1);
        assertTrue(model.getAdditionalFunctions().get(0).getFunctionName().equals("link"));
        assertTrue(model.getRestrict().equals("\"EA\""));


    }


}
