import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.junit.Test;
import probes.EnumProbe;
import rest.*;
import supportive.reflectRefact.IntellijDtoReflector;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EnumTest extends LightCodeInsightFixtureTestCase {

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

        myFixture.configureByFiles("EnumProbe.java");
        PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.getFile();

        List<GenericClass>  retList = IntellijDtoReflector.reflectDto(Arrays.asList(psiJavaFile.getClasses()), psiJavaFile.getClasses()[0].getQualifiedName());

        assertTrue(retList.size() == 1);
        assertTrue(retList.get(0) instanceof GenericEnum);
        assertTrue(((GenericEnum)retList.get(0)).getAttributes().size() == 3);

        ((GenericEnum) retList.get(0)).getAttributes().stream().map(attr -> EnumProbe.valueOf(attr)).collect(Collectors.toList());
    }

}
