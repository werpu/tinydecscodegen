package actions_ng;

import actions.CreateTnDecComponent;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import dtos.ComponentAttribute;
import factories.TnDecGroupFactory;
import reflector.NgComponentAttributesReflector;
import reflector.NgTransclusionReflector;

import java.util.List;

public class CreateNgComponent extends CreateTnDecComponent {

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
    protected List<ComponentAttribute> getCompAttrs(Editor editor, gui.CreateTnDecComponent mainForm) {
        return NgComponentAttributesReflector.reflect(editor.getDocument().getText(), mainForm.getControllerAs());
    }

    @Override
    protected boolean isAngular1() {
        return false;
    }
}
