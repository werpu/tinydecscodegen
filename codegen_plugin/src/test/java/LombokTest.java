import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.junit.Test;
import rest.GenericClass;
import supportive.reflectRefact.IntellijDtoReflector;
import util.TestUtils;

import java.util.Arrays;
import java.util.List;

public class LombokTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected String getTestDataPath() {
        return TestUtils.JAVA_TEST_PROBES_PATH;
    }

    @Test
    public void testFirstFile() {
        myFixture.configureByFiles("LombokProbe1.java");
        PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.getFile();

        assertTrue(psiJavaFile != null);
        assertTrue(psiJavaFile.getClasses()[0].getQualifiedName().contains("LombokProbe1"));


        List<GenericClass> dtos = IntellijDtoReflector.reflectDto(Arrays.asList(psiJavaFile.getClasses()), "");

        assertTrue(dtos.size() == 1);
        assertTrue(dtos.get(0).getProperties().size() == 2);

    }

    @Test
    public void testSecondFile() {
        myFixture.configureByFiles("LombokProbe2.java");
        PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.getFile();

        assertTrue(psiJavaFile != null);
        assertTrue(psiJavaFile.getClasses()[0].getQualifiedName().contains("LombokProbe2"));


        List<GenericClass> dtos = IntellijDtoReflector.reflectDto(Arrays.asList(psiJavaFile.getClasses()), "");

        assertTrue(dtos.size() == 1);
        assertTrue(dtos.get(0).getProperties().size() == 2);

    }

    @Test
    public void testThirdFile() {
        myFixture.configureByFiles("LombokProbe3.java");
        PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.getFile();

        assertTrue(psiJavaFile != null);
        assertTrue(psiJavaFile.getClasses()[0].getQualifiedName().contains("LombokProbe3"));


        List<GenericClass> dtos = IntellijDtoReflector.reflectDto(Arrays.asList(psiJavaFile.getClasses()), "");

        assertTrue(dtos.size() == 1);
        assertTrue(dtos.get(0).getProperties().size() == 1);

    }
}
