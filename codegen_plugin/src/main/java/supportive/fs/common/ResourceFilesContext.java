package supportive.fs.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * a compound context bundling all
 * other contextes for our resource view
 */
@Getter
public class ResourceFilesContext extends IntellijFileContext {

    List<NgModuleFileContext> modules;
    List<ComponentFileContext> components;
    List<ComponentFileContext> controllers;
    List<IUIRoutesRoutesFileContext> routes;
    List<ServiceContext> services;
    List<FilterPipeContext> filtersPipes;

    public ResourceFilesContext(AnActionEvent event) {
        super(event);
        reset();
    }

    public ResourceFilesContext(Project project) {
        super(project);
        reset();
    }



    public void addModule(NgModuleFileContext ctx) {
        modules.add(ctx);
    }

    public void addComponent(ComponentFileContext ctx) {
        components.add(ctx);
    }

    public void addController(ComponentFileContext ctx) {
        controllers.add(ctx);
    }

    public void addRoute(IUIRoutesRoutesFileContext ctx) {
        routes.add(ctx);
    }

    public void addService(ServiceContext ctx) {
        services.add(ctx);
    }

    public void addFilters(FilterPipeContext ctx) {
        filtersPipes.add(ctx);
    }
    
    public void reset() {
        modules = new ArrayList<>();
        components = new ArrayList<>();
        controllers = new ArrayList<>();
        routes = new ArrayList<>();
        services = new ArrayList<>();
        filtersPipes = new ArrayList<>();
    }
}
