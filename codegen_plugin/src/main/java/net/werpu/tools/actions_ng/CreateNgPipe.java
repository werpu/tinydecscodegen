package net.werpu.tools.actions_ng;

import net.werpu.tools.actions.CreateTnDecFilter;
import net.werpu.tools.actions_all.shared.GenerateFileAndAddRef;
import net.werpu.tools.actions_all.shared.NgFileNameTransformer;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.werpu.tools.factories.TnDecGroupFactory;
import org.jetbrains.annotations.NotNull;
import net.werpu.tools.supportive.dtos.ModuleElementScope;

import java.util.Map;

public class CreateNgPipe extends CreateTnDecFilter {

    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.ngVisible(anActionEvent);
    }

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


    @NotNull
    @Override
    protected String getExportLabel() {
        return "Export Pipe";
    }

    @NotNull
    @Override
    protected String getDialogTitle() {
        return getTitle();
    }
}
