package actions_ng;

import actions.CreateTnDecFilter;
import actions.shared.GenerateFileAndAddRef;
import actions.shared.NgFileNameTransformer;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import factories.TnDecGroupFactory;
import org.jetbrains.annotations.NotNull;
import utils.ModuleElementScope;

import java.util.Map;

public class CreateNgPipe extends CreateTnDecFilter {

    @NotNull
    @Override
    protected String getTitle() {
        return "Create Pipe";
    }

    protected FileTemplate getTemplate(Project project) {
        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_NG_PIPE);
    }

    @Override
    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new NgFileNameTransformer("pipe"), ModuleElementScope.DECLARATIONS, ModuleElementScope.EXPORT).run();
    }
}
