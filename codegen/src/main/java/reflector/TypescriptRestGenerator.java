package reflector;


import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import rest.RestService;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TypescriptRestGenerator {


    public static String generate(String outDir, List<RestService> restServices) {

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);


        ClassTemplateLoader ctl = new ClassTemplateLoader(TypescriptRestGenerator.class, "/");
        cfg.setTemplateLoader(ctl);


        cfg.setDefaultEncoding("UTF-8");


        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        cfg.setLogTemplateExceptions(false);
        return generate(cfg, restServices);
    }


    public static String generate(Configuration cfg, List<RestService> restServices)  {


        return restServices.parallelStream().map((RestService item) -> {
            try {

                Map<String, Object> root = new HashMap<>();
                root.put("service", item);
                Template tpl = cfg.getTemplate("serviceTemplate.ftl");
                StringWriter w = new StringWriter();
                tpl.process(root, w);
                return w.toString();
            } catch (TemplateException ex) {
                return ex.getMessage();
            } catch (IOException e) {
                return e.getMessage();
            }
        }).reduce("", (s0, s1) -> s0 + "\n" + s1);
    }


}
