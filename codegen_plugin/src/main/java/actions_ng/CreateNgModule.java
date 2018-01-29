package actions_ng;

import actions.CreateTnDecModule;
import actions.shared.GenerateFileAndAddRef;
import actions.shared.NgFileNameTransformer;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import factories.TnDecGroupFactory;
import utils.ModuleElementScope;

import java.util.Map;


/**
 * Create an Angular 2 module
 */
public class CreateNgModule extends CreateTnDecModule {

    @Override
    protected FileTemplate getJ2eeTemplate(Project project) {
        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_NG_MODULE);
    }

    @Override
    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new NgFileNameTransformer("module"), ModuleElementScope.IMPORT).run();
    }

    @Override
    protected String getModuleName(String className) {
        NgFileNameTransformer transformer = new NgFileNameTransformer("");

        String transformed = transformer.transform(className).replaceAll("\\.\\.ts", "");
        if(!transformed.endsWith("-module")) {
            transformed = transformed+"-module";
        }
        return transformed.toLowerCase();
    }
}
