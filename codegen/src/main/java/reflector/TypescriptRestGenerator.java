/*

Copyright 2017 Werner Punz

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software
is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
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

/**
 * The Rest generator for typescript utilizing
 * the model built up by the rest service
 */
public class TypescriptRestGenerator {

    /**
     * Main entry point for the generator
     *
     * @param restServices the parsing model which needs to be templated
     *
     * @return a string with the service or errors in case of parsing errors
     */
    public static String generate(List<RestService> restServices, boolean ng) {

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);


        ClassTemplateLoader ctl = new ClassTemplateLoader(TypescriptRestGenerator.class, "/");
        cfg.setTemplateLoader(ctl);


        cfg.setDefaultEncoding("UTF-8");


        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        cfg.setLogTemplateExceptions(false);
        if(ng) {
            return generateNg(cfg,restServices );
        } else {
            return generate(cfg, restServices);
        }
    }

    /**
     * the single file generation utilizing freemarker
     *
     * @param cfg the freemarker config
     * @param restServices a list of services models which need to be templated
     * @return a concatenated string of the services being processed
     */
    private static String generate(Configuration cfg, List<RestService> restServices)  {

        return restServices.stream().map((RestService item) -> {
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



    /**
     * the single file generation utilizing freemarker
     *
     * @param cfg the freemarker config
     * @param restServices a list of services models which need to be templated
     * @return a concatenated string of the services being processed
     */
    private static String generateNg(Configuration cfg, List<RestService> restServices)  {

        return restServices.stream().map((RestService item) -> {
            try {

                Map<String, Object> root = new HashMap<>();
                root.put("service", item);
                Template tpl = cfg.getTemplate("ngServiceTemplate.ftl");
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
