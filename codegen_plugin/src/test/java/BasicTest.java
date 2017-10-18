import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.junit.Test;
import probes.TestDto;
import reflector.SpringJavaRestReflector;
import reflector.utils.ReflectUtils;
import reflector.utils.TypescriptTypeMapper;
import rest.GenericClass;
import utils.IntellijSpringJavaRestReflector;

import java.util.*;

import static org.junit.Assert.assertTrue;

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


        List<GenericClass> dtos = IntellijSpringJavaRestReflector.reflectDto(Arrays.asList(psiJavaFile.getClasses()), "");


    }

    @Test
    public void testDtoReflection() {
        myFixture.configureByFiles("TestDto.java");
        PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.getFile();

        List<GenericClass> genericClasses = IntellijSpringJavaRestReflector.reflectDto(Arrays.asList( psiJavaFile.getClasses()), "");
        assertTrue(genericClasses.size() == 1);
        GenericClass parsedClass = genericClasses.get(0);
        assertTrue(parsedClass.getName().equals("TestDto"));

        assertTrue(parsedClass.getProperties().size() == 3);

        assertTrue(parsedClass.getProperties().get(2).getName().equals("retVal"));
        assertTrue(parsedClass.getProperties().get(2).getClassType().toTypescript(TypescriptTypeMapper::map, ReflectUtils::reduceClassName).equals("ProbeRetVal"));

        assertTrue(parsedClass.getProperties().get(0).getClassType().hasJavaType(true));

        assertTrue(parsedClass.getProperties().get(2).getClassType().hasExtendedType(true));

        assertTrue(parsedClass.getProperties().get(2).getClassType().getNonJavaTypes(true).get(0).getTypeName().endsWith("ProbeRetVal"));
    }


}
