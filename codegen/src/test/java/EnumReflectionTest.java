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
