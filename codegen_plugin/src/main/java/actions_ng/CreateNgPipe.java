package actions_ng;

import actions.CreateTnDecFilter;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import factories.TnDecGroupFactory;
import org.jetbrains.annotations.NotNull;

public class CreateNgPipe extends CreateTnDecFilter {

    @NotNull
    @Override
    protected String getTitle() {
        return "Create Pipe";
    }

    protected FileTemplate getTemplate(Project project) {
        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_NG_PIPE);
    }
}
