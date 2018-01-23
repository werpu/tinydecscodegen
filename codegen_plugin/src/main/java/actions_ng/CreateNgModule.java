package actions_ng;

import actions.CreateTnDecModule;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import factories.TnDecGroupFactory;

public class CreateNgModule extends CreateTnDecModule {

    @Override
    protected FileTemplate getJ2eeTemplate(Project project) {
        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_NG_MODULE);
    }
}
