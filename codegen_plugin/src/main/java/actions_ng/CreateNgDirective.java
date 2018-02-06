package actions_ng;

import actions.CreateTnDecDirective;
import actions.shared.GenerateFileAndAddRef;
import actions.shared.NgFileNameTransformer;
import com.google.common.collect.Lists;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import factories.TnDecGroupFactory;
import supportive.dtos.ModuleElementScope;

import java.util.List;
import java.util.Map;

public class CreateNgDirective extends CreateTnDecDirective {

    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        List<ModuleElementScope> scope = Lists.newArrayList();
        scope.add(ModuleElementScope.DECLARATIONS);
        if(attrs.containsKey(EXPORT)) {
            scope.add(ModuleElementScope.EXPORT);
        }

        ModuleElementScope[] scopes = scope.stream().toArray(size -> new ModuleElementScope[size]);
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new NgFileNameTransformer("directive"), scopes).run();
    }

    protected FileTemplate getFileTemplate(Project project) {
        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_NG_DIRECTIVE);
    }

    @Override
    protected boolean isAngular1() {
        return false;
    }
}
