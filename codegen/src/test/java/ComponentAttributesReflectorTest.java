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


