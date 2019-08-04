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
import org.junit.Test;
import net.werpu.tools.providers.NavTreeStructureProvider;
import net.werpu.tools.providers.RouteTreeNode;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.PsiRouteContext;
import net.werpu.tools.supportive.fs.ng.NG_UIRoutesRoutesFileContext;
import net.werpu.tools.supportive.fs.tn.TNAngularRoutesFileContext;

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

        NG_UIRoutesRoutesFileContext ctx = new NG_UIRoutesRoutesFileContext(prj, fs);

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

       TNAngularRoutesFileContext ctx = new TNAngularRoutesFileContext(prj, fs);

        List<PsiRouteContext> routes = ctx.getRoutes();
        assertTrue(routes.size() > 2);




    }


   /* public void testTnUiRoutesTree() {
        if(!assertTestable()) {
            return;
        }
        PsiFile[] files = myFixture.configureByFiles("TN_UIRoutesRoutes.ts", "module1/View1.ts", "module2/View2.ts");
        fs = files[0];

        TNAngularRoutesFileContext ctx = new TNAngularRoutesFileContext(prj, fs);

        List<PsiRouteContext> routes = ctx.getRoutes();
        assertTrue(routes.size() > 2);




    }*/

}
