import dtos.ArgumentType;
import dtos.ComponentAttribute;
import org.junit.Test;
import probes.TestDto;
import probes.TestProbeController;
import reflector.ComponentAttributesReflector;
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


public class ComponentAttributesReflectorTest {

        @Test
        public void testReflect() {
            String probe = "<h1 ng-click='ctrl.clickIt()' ng-click='ctrl.clickIt2($abc)'  ng-if='ctrl.testAttr'></h1>";

            List<ComponentAttribute> reflected = ComponentAttributesReflector.reflect(probe, "ctrl");

            assertTrue(reflected.size() == 3);

            assertTrue(reflected.contains(new ComponentAttribute("clickIt", ArgumentType.Func,"Function",  false)));
            assertTrue(reflected.contains(new ComponentAttribute("clickIt2", ArgumentType.Func,"Function", false)));

            assertTrue(reflected.contains(new ComponentAttribute("testAttr", ArgumentType.Input,"any", false)));
        }

}


