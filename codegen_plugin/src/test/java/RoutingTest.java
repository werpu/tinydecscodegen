import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.junit.Test;
import supportive.fs.common.ComponentFileContext;
import supportive.fs.common.PsiElementContext;
import supportive.fs.common.PsiRouteContext;
import supportive.fs.common.Route;
import supportive.fs.ng.NG_UIRoutesRoutesFileContext;
import supportive.fs.tn.TNAngularRoutesFileContext;
import supportive.fs.tn.TNUIRoutesFileContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class RoutingTest extends BaseTsTest {



    protected void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testTsReflection() throws Exception {
        if (!assertTestable()) {
            return;
        }


        NG_UIRoutesRoutesFileContext routesFileContext = new NG_UIRoutesRoutesFileContext(prj, fs);
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
        Route route = new Route("my.route", "/myroute", "MyComponent", this.getClass());

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

        Route route = new Route("my.route",  "/MyComponent", "MyComponent", "MyComponentVar","./MyComponent.ts", this.getClass());
        Route route2 = new Route("my.route2", "/view2", "MyComponent", this.getClass());



        fileContext.addRoute(route);

        assertFalse(fileContext.isRouteNameUsed(route));
        assertTrue(fileContext.isUrlInUse(route2));

        assertTrue(true);

        //not valid anymore, because the addRoute is in its own transaction now

        //assertTrue(fileContext.getRefactorUnits().size() == 2);

    }

    @Test
    public void testTNUIRouting() {
        if (!assertTestable()) {
            return;
        }
        PsiFile fs[] = myFixture.configureByFiles("Routes2.ts", "module2/View2.ts", "module1/View1.ts");


        Project prj = myFixture.getProject();
        TNUIRoutesFileContext fileContext = new TNUIRoutesFileContext(prj, fs[0]);

        assertTrue(fileContext.getRouteParams().size() == 5);


        List<PsiRouteContext> rets = fileContext.getRouteParams().stream()
                .map(fileContext::parse)
                .flatMap(el -> el.stream())
                .collect(Collectors.toList());

        assertTrue(true);

    }


    @Test
    public void testComponentParsing() {
        if (!assertTestable()) {
            return;
        }
        PsiFile[] fs = myFixture.configureByFiles("pages/main-page.component.ts","pages/main-page.component.html");
        Project prj = myFixture.getProject();

        ComponentFileContext componentFileContext = new ComponentFileContext(prj, fs[0]);

        Optional<String> classNameEl = componentFileContext.findComponentClassName();

        assertTrue(classNameEl.isPresent() && classNameEl.get().equals("MainPageComponent"));


    }

}


