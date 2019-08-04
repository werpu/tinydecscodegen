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
import probes.EnumProbe;
import reflector.ComponentAttributesReflector;
import reflector.SpringJavaRestReflector;
import reflector.utils.ReflectUtils;
import rest.GenericClass;
import rest.GenericEnum;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class EnumReflectionTest {

    @Test
    public void testEnumReflection()  {
        List<GenericClass>  retList = SpringJavaRestReflector.reflectDto(Arrays.asList(EnumProbe.class), EnumProbe.class);
        assertTrue(retList.size() == 1);
        assertTrue(retList.get(0) instanceof GenericEnum);
        assertTrue(((GenericEnum)retList.get(0)).getAttributes().size() == 3);
        //all values should be mappable
        ((GenericEnum) retList.get(0)).getAttributes().stream().map(attr -> EnumProbe.valueOf(attr)).collect(Collectors.toList());
    }
}
