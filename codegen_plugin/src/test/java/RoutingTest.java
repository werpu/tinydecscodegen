import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.junit.Test;
import supportive.fs.PsiElementContext;
import supportive.fs.ComponentFileContext;
import supportive.fs.UIRoutesRoutesFileContext;
import supportive.fs.Route;

import java.util.Optional;


public class RoutingTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    protected String getTestDataPath() {
        return "src/test/typescript/probes";
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
    public void testTsReflection() throws Exception {
        if (!assertTestable()) {
            return;
        }


        myFixture.allowTreeAccessForAllFiles();
        PsiFile fs = myFixture.configureByFile("Routes.ts");
        Project prj = myFixture.getProject();

        //TODO move this code to our main action

        //Collection<PsiFile> psiFiles = IntellijUtils.searchComments(myFixture.getProject(), "ts", "UIRouterModule.forRoot");


        UIRoutesRoutesFileContext routesFileContext = new UIRoutesRoutesFileContext(prj, fs);
        Optional<PsiElementContext> arr = routesFileContext.getNavigationalArray();

        assertTrue(arr.isPresent());

        Optional<PsiElementContext> brckt = routesFileContext.getEndOfNavArr();
        assertTrue(brckt.isPresent());

    }

    @Test
    public void testRouting() throws Exception {
        if (!assertTestable()) {
            return;
        }
        Route route = new Route("my.route", "/myroute", "MyComponent");

        assertTrue(route.toStringNg2().replaceAll("\\s", "").startsWith("letmy_route={name:'my.route',url:'/myroute',component:MyComponent}"));
    }



    @Test
    public void testComponentParsing() {
        if (!assertTestable()) {
            return;
        }
        PsiFile fs = myFixture.configureByFile("pages/main-page.component.ts");
        Project prj = myFixture.getProject();

        ComponentFileContext componentFileContext = new ComponentFileContext(prj, fs);

        Optional<String> classNameEl = componentFileContext.findComponentClassName();

        assertTrue(classNameEl.isPresent() && classNameEl.get().equals("MainPageComponent"));


    }

}


