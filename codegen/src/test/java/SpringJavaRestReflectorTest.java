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

import org.junit.Test;
import probes.subPackage.TestDto;
import probes.TestProbeController;
import reflector.SpringJavaRestReflector;
import reflector.utils.ReflectUtils;
import reflector.utils.TypescriptTypeMapper;
import rest.GenericClass;
import rest.RestMethod;
import rest.RestService;
import rest.RestVar;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class SpringJavaRestReflectorTest {



    @Test
    public void testBasicReflection() {
        List<RestService> services = SpringJavaRestReflector.reflectRestService(Arrays.asList(TestProbeController.class), true);
        assertTrue(services.size() == 1);

        RestService restService = services.get(0);
        assertTrue(restService.getServiceName().equals("TestProbeService"));
        assertTrue(restService.getServiceRootUrl().equals("rest/testprobe1"));

        assertTrue(restService.getMethods().size() == 5);


        //first method



        RestMethod method0 = restService.getMethods().get(0);
        RestMethod method1 = restService.getMethods().get(1);
        RestMethod method2 = restService.getMethods().get(2);
        RestMethod method3 = restService.getMethods().get(3);
        RestMethod method4 = restService.getMethods().get(4);

        assertTrue(method0.getName().equals("probeGet"));
        assertTrue(method0.getUrl().indexOf("approval/getit/resource") != -1);

        assertTrue(method0.getParams().size() == 3);
        assertParameters(method0);

        //return value assertion

        assertTrue(method2.getParams().size() == 3);

        Optional<RestVar> retVal0 = method0.getReturnValue();
        Optional<RestVar> retVal2 = method2.getReturnValue();
        RestVar param0 = method2.getParams().get(0);
        RestVar param1 = method2.getParams().get(1);
        RestVar param2 = method2.getParams().get(2);

        assertTrue(retVal2.isPresent());
        assertTrue(retVal2.get().isArray());

        assertTrue(param2.toTypeScript().equals("filter: string"));

        assertTrue(retVal0.get().toTypeScript(TypescriptTypeMapper::map, ReflectUtils::reduceClassName).equals("ProbeRetVal"));
        assertTrue(retVal2.get().toTypeScript(TypescriptTypeMapper::map, ReflectUtils::reduceClassName).equals("Array<ProbeRetVal>"));
        assertTrue(retVal2.get().getNonJavaTypes(true).get(0).getTypeName().endsWith("ProbeRetVal"));


        assertTrue(method3.getReturnValue().get().toTypeScript().equals("{[key:string]:ProbeRetVal}"));
        assertTrue(method4.getReturnValue().get().toTypeScript().equals("{[key:string]:{[key:string]:number}}"));
    }

    @Test
    public void testDtoReflection() {
        List<GenericClass> genericClasses = SpringJavaRestReflector.reflectDto(Arrays.asList( TestDto.class), TestDto.class);
        assertTrue(genericClasses.size() == 1);
        GenericClass parsedClass = genericClasses.get(0);
       assertTrue(parsedClass.getClazz().getChildTypes().size() > 0);
        assertTrue(parsedClass.getName().equals("TestDto"));

        assertTrue(parsedClass.getProperties().size() == 3);

        assertTrue(parsedClass.getProperties().get(2).getName().equals("retVal"));
        assertTrue(parsedClass.getProperties().get(2).getClassType().toTypescript(TypescriptTypeMapper::map, ReflectUtils::reduceClassName).equals("ProbeRetVal"));

        assertTrue(parsedClass.getProperties().get(0).getClassType().hasJavaType(true));

        assertTrue(parsedClass.getProperties().get(2).getClassType().hasExtendedType(true));

        assertTrue(parsedClass.getProperties().get(2).getClassType().getNonJavaTypes(true).get(0).getTypeName().endsWith("ProbeRetVal"));
    }

    private void assertParameters(RestMethod method0) {
        for(RestVar param: method0.getParams()) {
            assertTrue(param.getParamType().isPathVariable());
            assertTrue(param.getClassType().getTypeName().contains("String"));
            assertTrue(param.toTypeScript(TypescriptTypeMapper::map, ReflectUtils::reduceClassName).equals(param.getName()+": string"));
            assertFalse(param.isArray());
        }
    }
}
