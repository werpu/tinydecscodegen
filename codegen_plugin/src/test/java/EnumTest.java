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
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Test;
import probes.EnumProbe;
import rest.GenericClass;
import rest.GenericEnum;
import net.werpu.tools.supportive.reflectRefact.IntellijDtoReflector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static util.TestUtils.JAVA_TEST_PROBES_PATH;

public class EnumTest extends LightJavaCodeInsightFixtureTestCase {

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
