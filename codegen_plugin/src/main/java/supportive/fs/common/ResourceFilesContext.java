package supportive.fs.common;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public ResourceFilesContext(Project project, List<NgModuleFileContext> modules, List<ComponentFileContext> components, List<ComponentFileContext> controllers, List<IUIRoutesRoutesFileContext> routes, List<ServiceContext> services, List<FilterPipeContext> filtersPipes) {
        super(project);
        this.modules = modules;
        this.components = components;
        this.controllers = controllers;
        this.routes = routes;
        this.services = services;
        this.filtersPipes = filtersPipes;
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

    public Icon getIcon() {
        return AllIcons.Nodes.AbstractClass;
    }

    public ResourceFilesContext search(String in) {
        return new ResourceFilesContext(project,
                modules.stream().filter(e -> e.getDisplayName().toLowerCase().contains( in.toLowerCase())).collect(Collectors.toList()),
                components.stream().filter(e -> e.getDisplayName().toLowerCase().contains( in.toLowerCase())).collect(Collectors.toList()),
                controllers.stream().filter(e -> e.getDisplayName().toLowerCase().contains( in.toLowerCase())).collect(Collectors.toList()),
                Collections.emptyList(),
                //routes.stream().filter(e -> e.().toLowerCase().contains( in.toLowerCase())).collect(Collectors.toList()),
                services.stream().filter(e -> e.getDisplayName().toLowerCase().contains( in.toLowerCase())).collect(Collectors.toList()),
                filtersPipes.stream().filter(e -> e.getDisplayName().toLowerCase().contains( in.toLowerCase())).collect(Collectors.toList()));
    }
}
