import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.junit.Test;
import supportive.fs.common.IntellijFileContext;
import supportive.fs.common.PsiElementContext;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static supportive.reflectRefact.PsiWalkFunctions.PSI_ELEMENT_JS_IDENTIFIER;
import static util.TestUtils.JS_TEST_PROBES_PATH;

public class PsiQueryTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    protected String getTestDataPath() {
        return JS_TEST_PROBES_PATH;
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
    public void testTNRouting() throws Exception {
        if (!assertTestable()) {
            return;
        }
        PsiFile psiFile = myFixture.configureByFile("TN_Routes.ts");
        Project project = myFixture.getProject();

        IntellijFileContext fileContext = new IntellijFileContext(project, psiFile);

        Stream<PsiElementContext> routeProviderQuery = fileContext.queryContent(PSI_ELEMENT_JS_IDENTIFIER, "TEXT:($routeProvider)");
        Optional routeProvider = routeProviderQuery.findFirst();
        assertTrue(routeProvider.isPresent());
        routeProviderQuery = fileContext.queryContent(PSI_ELEMENT_JS_IDENTIFIER, "TEXT:($routeProvider)");
        assertTrue(routeProviderQuery.collect(Collectors.toList()).size() == 4);


    }
}
