import dtos.ArgumentType;
import dtos.ComponentAttribute;
import org.junit.Test;
import reflector.ComponentAttributesReflector;
import reflector.NgComponentAttributesReflector;

import java.util.List;

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

        @Test
        public void testReflectNg() {
            String probe = "<h1 (click)='clickIt()' (click)='clickIt2($abc)'>{{testAttr}}</h1>";
            List<ComponentAttribute> reflected = NgComponentAttributesReflector.reflect(probe, "ctrl");

            assertTrue(reflected.size() == 3);

            assertTrue(reflected.contains(new ComponentAttribute("clickIt", ArgumentType.Func,"Function",  false)));
            assertTrue(reflected.contains(new ComponentAttribute("clickIt2", ArgumentType.Func,"Function", false)));

            assertTrue(reflected.contains(new ComponentAttribute("testAttr", ArgumentType.Input,"any", false)));

        }

}


