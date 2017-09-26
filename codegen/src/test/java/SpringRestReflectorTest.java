import rest.RestMethod;
import rest.RestService;
import rest.RestVar;
import org.junit.Test;
import probes.TestProbeController;
import reflector.SpringRestReflector;
import reflector.utils.ReflectUtils;
import reflector.utils.TypescriptTypeMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class SpringRestReflectorTest {



    @Test
    public void testBasicReflection() {
        List<RestService> services = SpringRestReflector.reflect(Arrays.asList(TestProbeController.class), true);
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

        assertTrue(param2.toTypeScript().equals("arg2: string"));

        assertTrue(retVal0.get().toTypeScript(TypescriptTypeMapper::map, ReflectUtils::reduceClassName).equals("ProbeRetVal"));
        assertTrue(retVal2.get().toTypeScript(TypescriptTypeMapper::map, ReflectUtils::reduceClassName).equals("Array<ProbeRetVal>"));

        assertTrue(method3.getReturnValue().get().toTypeScript().equals("{[key:string]:ProbeRetVal}"));
        assertTrue(method4.getReturnValue().get().toTypeScript().equals("{[key:string]:{[key:string]:number}}"));
    }

    private void assertParameters(RestMethod method0) {
        for(RestVar param: method0.getParams()) {
            assertTrue(param.getParamType().isPathVariable());
            assertTrue(((Class) param.getClassType()).getName().contains("String"));
            assertTrue(param.toTypeScript(TypescriptTypeMapper::map, ReflectUtils::reduceClassName).equals(param.getName()+": string"));
            assertFalse(param.isArray());
        }
    }
}
