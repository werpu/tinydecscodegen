import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.junit.Test;
import supportive.fs.common.ComponentFileContext;
import supportive.fs.common.PsiElementContext;
import supportive.fs.common.Route;
import supportive.fs.ng.UIRoutesRoutesFileContext;
import supportive.fs.tn.TNAngularRoutesFileContext;

import java.util.Optional;


public class RoutingTest extends BaseTsTest {



    protected void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testTsReflection() throws Exception {
        if (!assertTestable()) {
            return;
        }


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
    public void testTNRouting() throws Exception {
        if (!assertTestable()) {
            return;
        }
        PsiFile fs = myFixture.configureByFile("TN_Routes.ts");
        Project prj = myFixture.getProject();

        TNAngularRoutesFileContext fileContext = new TNAngularRoutesFileContext(prj, fs);

        assertTrue(fileContext.getConstructors().size() > 0);

        Route route = new Route("my.route",  "/MyComponent", "MyComponent", "MyComponentVar","./MyComponent.ts");
        Route route2 = new Route("my.route2", "/view2", "MyComponent");



        fileContext.addRoute(route);

        assertFalse(fileContext.isRouteNameUsed(route));
        assertTrue(fileContext.isUrlInUse(route2));

        assertTrue(true);
        assertTrue(fileContext.getRefactorUnits().size() == 2);

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


