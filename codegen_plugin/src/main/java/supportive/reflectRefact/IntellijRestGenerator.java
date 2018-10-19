package supportive.reflectRefact;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.project.Project;
import factories.TnDecGroupFactory;
import rest.RestService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static factories.TnDecGroupFactory.TPL_TN_NG_REST_SERVICE;

public class IntellijRestGenerator {
    /**
     * Main entry point for the generator
     *
     * @param restServices the parsing model which needs to be templated
     *
     * @return a string with the service or errors in case of parsing errors
     */
    public static String generate(Project project, List<RestService> restServices, boolean ng) {
        final FileTemplate vslTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(ng ? TPL_TN_NG_REST_SERVICE : TnDecGroupFactory.TPL_TN_REST_SERVICE);

        return restServices.stream().map((RestService item) -> {
            try {

                Map<String, Object> root = new HashMap<>();
                root.put("service", item);

                return FileTemplateUtil.mergeTemplate(root, vslTemplate.getText(), false);

            } catch (IOException e) {
                return e.getMessage();
            }
        }).reduce("", (s0, s1) -> s0 + "\n" + s1);
    }
}
