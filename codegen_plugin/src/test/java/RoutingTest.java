import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.*;
import utils.IntellijUtils;
import utils.PsiElementContext;
import utils.StringUtils;
import utils.fs.Route;
import utils.fs.TypescriptFileContext;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.Assume.assumeTrue;


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


        //myFixture.configureByFiles("typescript/probes/MainApp.ts", "typescript/probes/Routes.ts");
        // PsiFile psiJavaFile = myFixture.getProject().getWorkspaceFile();

        Project prj = myFixture.getProject();
        Collection<PsiFile> psiFiles = IntellijUtils.searchFiles(myFixture.getProject(), "ts", "UIRouterModule.forRoot");

//        myModule.getModuleFile().getChildren()


        Optional<PsiElementContext> arr = getNavigationalArray(fs, prj);

        assertTrue(arr.isPresent());


        Optional<PsiElementContext> brckt = findEndOfArr(arr.get());
        assertTrue(brckt.isPresent());

        // FileType ts = FileTypeManagerEx.getInstanceEx().getFileTypeByExtension("ts");

        //PsiFile psiFile = super.(ts, "alert('hello');");

        //assertTrue(routes.size() > 0);


    }

    @Test
    public void testRouting() throws Exception {
        Route route = new Route("my.route", "/myroute", "MyComponent");

        assertTrue(route.toStringNg2().replaceAll("\\s", "").startsWith("letmy_route={name:'my.route',url:'/myroute',component:MyComponent}"));
    }


    @NotNull
    public Optional<PsiElementContext> getNavigationalArray(PsiFile fs, Project prj) {
        TypescriptFileContext tsContext = new TypescriptFileContext(prj, fs);
        List<PsiElement> els = tsContext.findPsiElements(el -> {
            return (el.toString().equals("JSCallExpression")) && el.getText().startsWith("UIRouterModule.forRoot");
        });
        return els.stream().map(el -> new PsiElementContext(el))
                .flatMap(elc -> elc.findPsiElements(el -> el.toString().equals("JSProperty") && StringUtils.elVis(el, "nameIdentifier", "text").isPresent() && StringUtils.elVis(el, "nameIdentifier", "text").get().equals("states")).stream())
                .map(elc -> elc.findPsiElement(el -> el.toString().equals("JSArrayLiteralExpression"))).findFirst().get();
    }

    public Optional<PsiElementContext> findEndOfArr(PsiElementContext navArr) {


        //find the last closing bracket
        PsiElementContext closingBracket = navArr
                .findPsiElements(el -> el.toString().equals("PsiElement(JS:RBRACKET)")).stream() //TODO type check once debugged out
                .reduce((first, second) -> second).orElse(null);
        return Optional.ofNullable(closingBracket);
    }


}


