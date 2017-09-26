import org.apache.velocity.runtime.parser.ParseException;
import rest.RestService;
import org.junit.Test;
import probes.TestProbeController;
import reflector.SpringRestReflector;
import reflector.TypescriptRestGenerator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;



public class TemplateTest {
    @Test
    public void testBasicTemplating() throws IOException, ParseException {
        List<RestService> services = SpringRestReflector.reflect(Arrays.asList(TestProbeController.class), true);
        TypescriptRestGenerator.generate("booga", services);

        assertTrue(true);
    }
}
