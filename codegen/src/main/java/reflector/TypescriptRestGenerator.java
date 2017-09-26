package reflector;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import rest.RestService;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class TypescriptRestGenerator {

    static VelocityEngine ve = new VelocityEngine();

    static Template template;
    public static String generate(String outDir, List<RestService> restServices) throws ParseException, IOException {

        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        //ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,"/Users/werpu2/development/workspace/tinydecscodegen/codegen_plugin/src/");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
       // ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
        ve.init();

        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();

        InputStream str = TypescriptRestGenerator.class.getResourceAsStream("/serviceTemplate.vm");



        SimpleNode node = runtimeServices.parse(read(str), "Template name");
        template = new Template();
        template.setRuntimeServices(runtimeServices);
        template.setData(node);
        template.initDocument();

        return restServices.parallelStream().map(TypescriptRestGenerator::templateService).reduce("",(s0, s1) -> s0+"\n"+s1);
    }

    public static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

    private static String templateService(RestService item) {
        VelocityContext context = new VelocityContext();
        context.put("service", item);

        StringWriter w = new StringWriter();



        template.merge( context, w );

        System.out.println(w);
        return w.toString();
    }

    /*public static String generate(String outDir, List<RestService> restServices) {

        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, this.getClass().);
        ve.init();



        return restServices.stream().map(TypescriptRestGenerator::templateService).reduce("",(s0, s1) -> s0+"\n"+s1);
    }

    private static String  templateService(RestService item) {
        VelocityContext context = new VelocityContext();
        context.put("service", item);

        StringWriter w = new StringWriter();

        Template t = ve.getTemplate( "serviceTemplate.vm" );
        t.merge( context, w );

        System.out.println(" result : " + w );
        return w.toString();
    }*/

}
