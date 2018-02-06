package actions_ng;

import actions.CreateTnDecService;
import actions.shared.GenerateFileAndAddRef;
import actions.shared.NgFileNameTransformer;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import factories.TnDecGroupFactory;
import supportive.dtos.ModuleElementScope;

import java.util.Map;

/**
 * Service generation for Angular2
 */
public class CreateNgService extends CreateTnDecService {

    @Override
    protected FileTemplate getJ2eeTemplate(Project project) {

         return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_NG_SERVICE);
    }

    @Override
    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new NgFileNameTransformer("service"), ModuleElementScope.PROVIDERS).run();
    }
}
