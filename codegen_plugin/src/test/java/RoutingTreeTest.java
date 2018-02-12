import org.junit.Test;
import supportive.fs.common.PsiElementContext;
import supportive.fs.common.PsiRouteContext;
import supportive.fs.ng.UIRoutesRoutesFileContext;

import java.util.List;

/**
 * Test for our routing tree definitions
 * which form the model for our routing tree tools window
 * (and later angular artifact tools window)
 */
public class RoutingTreeTest extends BaseTsTest {

    protected void setUp() throws Exception {
        super.setUp();
        myFixture.allowTreeAccessForAllFiles();
        fs = myFixture.configureByFile("Routes.ts");
        prj = myFixture.getProject();
    }




    @Test
    public void testNgTree() {
        if(!assertTestable()) {
            return;
        }
        fs = myFixture.configureByFile("Routes.ts");

        UIRoutesRoutesFileContext ctx = new UIRoutesRoutesFileContext(prj, fs);

        List<PsiElementContext>  idents = ctx.getImportIdentifiers("MainPageComponent");
        assertTrue(idents.size() == 1);



        List<PsiRouteContext> routes = ctx.getRoutes();
        assertTrue(routes.size() > 2);
    }


}
