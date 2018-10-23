package supportive.fs.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import java.util.List;

/**
 * a compound context bundling all
 * other contextes for our resource view
 */
public class ResourceFilesContext extends IntellijFileContext {

    List<NgModuleFileContext> modules;
    List<ComponentFileContext> components;
    List<ComponentFileContext> views;
    List<PsiRouteContext> routes;
    List<ServiceContext> services;
    List<FilterPipeContext> filtersPipes;

    public ResourceFilesContext(AnActionEvent event) {
        super(event);
    }

    public ResourceFilesContext(Project project) {
        super(project);
    }
}
