import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.junit.Test;
import probes.EnumProbe;
import rest.GenericClass;
import rest.GenericEnum;
import supportive.reflectRefact.IntellijDtoReflector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static util.TestUtils.JAVA_TEST_PROBES_PATH;

public class EnumTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected String getTestDataPath() {
        return JAVA_TEST_PROBES_PATH;
    }

    @Test
    public void testFirstFile() {

        myFixture.configureByFiles("EnumProbe.java");
        PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.getFile();

        List<GenericClass>  retList = IntellijDtoReflector.reflectDto(Arrays.asList(psiJavaFile.getClasses()), psiJavaFile.getClasses()[0].getQualifiedName());

        assertTrue(retList.size() == 1);
        assertTrue(retList.get(0) instanceof GenericEnum);
        assertTrue(((GenericEnum)retList.get(0)).getAttributes().size() == 3);

        ((GenericEnum) retList.get(0)).getAttributes().stream().map(attr -> EnumProbe.valueOf(attr)).collect(Collectors.toList());
    }

}
