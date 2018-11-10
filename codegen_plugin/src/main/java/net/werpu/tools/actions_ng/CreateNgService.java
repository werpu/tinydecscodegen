package net.werpu.tools.actions_ng;

import net.werpu.tools.actions.CreateTnDecService;
import net.werpu.tools.actions_all.shared.GenerateFileAndAddRef;
import net.werpu.tools.actions_all.shared.NgFileNameTransformer;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.werpu.tools.factories.TnDecGroupFactory;
import net.werpu.tools.supportive.dtos.ModuleElementScope;

import java.util.Map;

/**
 * Service generation for Angular2
 */
public class CreateNgService extends CreateTnDecService {

    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.ngVisible(anActionEvent);
    }

    @Override
    protected FileTemplate getJ2eeTemplate(Project project) {

        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_NG_SERVICE);
    }

    @Override
    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new NgFileNameTransformer("service"), ModuleElementScope.PROVIDERS).run();
    }
}
