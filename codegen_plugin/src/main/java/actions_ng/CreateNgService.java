package actions_ng;

import actions.CreateTnDecService;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import factories.TnDecGroupFactory;

/**
 * Service generation for Angular2
 */
public class CreateNgService extends CreateTnDecService {

    @Override
    protected FileTemplate getJ2eeTemplate(Project project) {

         return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_NG_SERVICE);
    }
}
