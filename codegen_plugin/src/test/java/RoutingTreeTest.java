import com.intellij.psi.PsiFile;
import org.junit.Test;
import providers.NavTreeStructureProvider;
import providers.RouteTreeNode;
import supportive.fs.common.PsiElementContext;
import supportive.fs.common.PsiRouteContext;
import supportive.fs.ng.UIRoutesRoutesFileContext;
import supportive.fs.tn.TNUIRoutesRoutesFileContext;

import java.util.List;
import java.util.Optional;

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

        assertTrue(routes.stream().filter(route -> route.getRoute().getRouteKey().equals("mainpage.substate")).findFirst().isPresent());

        NavTreeStructureProvider navTree = new NavTreeStructureProvider(ctx);

        List<RouteTreeNode> treeNodes = navTree.getTreeNodes();
        assertTrue(!treeNodes.isEmpty());
        Optional<RouteTreeNode> subElem = treeNodes.stream()
                .filter(item -> item.getValue().getRoute().getRouteKey().equals("mainpage"))
                .findFirst()
                .get().getChildren()
                .stream().findFirst();
        assertTrue(subElem.get().getValue().getRoute().getRouteKey().equals("mainpage.substate"));

        assertTrue(treeNodes.stream().filter(item-> item.getValue().getRoute().getRouteKey().equals("firstpage")).findFirst().isPresent());
    }

    public void testTnTree() {
        if(!assertTestable()) {
            return;
        }
        PsiFile[] files = myFixture.configureByFiles("TN_Routes.ts", "module1/View1.ts", "module2/View2.ts");
        fs = files[0];

       TNUIRoutesRoutesFileContext ctx = new TNUIRoutesRoutesFileContext(prj, fs);
       //ctx.getRoutes();

    }

}
