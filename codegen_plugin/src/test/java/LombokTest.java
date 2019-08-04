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

import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.junit.Test;
import rest.GenericClass;
import net.werpu.tools.supportive.reflectRefact.IntellijDtoReflector;
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
