import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import net.werpu.tools.supportive.fs.common.ComponentFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import org.junit.Test;

import static util.TestUtils.JS_TEST_PROBES_PATH;

public class ComponentTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    protected String getTestDataPath() {
        return JS_TEST_PROBES_PATH;
    }


    @Test
    public void testController() {
        PsiFile componentFile = myFixture.configureByFile("module1/View1.ts");
        ComponentFileContext ctx = new ComponentFileContext(new IntellijFileContext(componentFile.getProject(), componentFile));
        assertTrue(ctx.getTagName().equals("view1"));
        assertTrue(ctx.getDisplayName().equals("View1"));
        //assertTrue(ctx.get().equals("View1"));
        assertTrue(ctx.getTemplateText().isPresent());

    }

    @Test
    public void testController2() {
        myFixture.configureByFile("pages/main-page.component.html");
        PsiFile componentFile = myFixture.configureByFile("pages/main-page.component.ts");
        ComponentFileContext ctx = new ComponentFileContext(new IntellijFileContext(componentFile.getProject(), componentFile));
        assertTrue(ctx.getTagName().equals("app-main-page"));
        assertTrue(ctx.getClazzName().equals("MainPageComponent"));
        //assertTrue(ctx.get().equals("View1"));
        assertTrue(ctx.getTemplateRef().isPresent());

    }



}
