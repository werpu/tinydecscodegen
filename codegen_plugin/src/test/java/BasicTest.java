import com.intellij.openapi.application.ex.PathManagerEx;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.JavaPsiFacadeEx;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.intellij.testFramework.IdeaTestCase;

public class BasicTest extends LightCodeInsightFixtureTestCase {

    @Override

    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/java/probes";
    }

    @Test
   public void testFirstFile() {

        myFixture.configureByFiles("TestDto.java");
        PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.getFile();

        assertTrue(psiJavaFile != null);
        assertTrue(psiJavaFile.getClasses()[0].getQualifiedName().contains("TestDto"));
  }

}
