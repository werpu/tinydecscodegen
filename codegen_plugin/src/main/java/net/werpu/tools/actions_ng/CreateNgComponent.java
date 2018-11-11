package net.werpu.tools.actions_ng;

import net.werpu.tools.actions.CreateTnDecComponent;
import net.werpu.tools.actions_all.shared.GenerateFileAndAddRef;
import net.werpu.tools.actions_all.shared.NgFileNameTransformer;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import com.google.common.collect.Lists;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import dtos.ComponentAttribute;
import net.werpu.tools.factories.TnDecGroupFactory;
import reflector.NgComponentAttributesReflector;
import reflector.NgTransclusionReflector;
import net.werpu.tools.supportive.dtos.ModuleElementScope;

import java.util.List;
import java.util.Map;

public class CreateNgComponent extends CreateTnDecComponent {

    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.ngVisible(anActionEvent);
    }

    @Override
    protected List<String> getPossibleTransclusionSlots(String templateText) {
        return NgTransclusionReflector.getPossibleTransclusionSlots(templateText);
    }

    @Override
    protected boolean hasTransclude(String templateText) {
        return NgTransclusionReflector.hasTransclude(templateText);
    }

    @Override
    protected FileTemplate getTemplate(Project project) {
        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_NG_COMPONENT);
    }

    @Override
    protected List<ComponentAttribute> getCompAttrs(Editor editor, net.werpu.tools.gui.CreateTnDecComponent mainForm) {
        return NgComponentAttributesReflector.reflect(editor.getDocument().getText(), mainForm.getControllerAs());
    }

    @Override
    protected boolean isAngular1() {
        return false;
    }

    @Override
    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        List<ModuleElementScope> scope = Lists.newArrayList();

        if (attrs.containsKey(EXPORT)) {
            scope.add(ModuleElementScope.EXPORT);
        }

        scope.add(ModuleElementScope.DECLARATIONS);

        ModuleElementScope[] scope1 = scope.stream().toArray(size -> new ModuleElementScope[size]);

        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new NgFileNameTransformer("component"), scope1).run();
    }
}