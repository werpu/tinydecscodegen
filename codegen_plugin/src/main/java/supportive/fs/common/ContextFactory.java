package supportive.fs.common;

import com.google.common.collect.Lists;
import indexes.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.ng.NG_UIRoutesRoutesFileContext;
import supportive.fs.tn.TNAngularRoutesFileContext;
import supportive.fs.tn.TNUIRoutesFileContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static supportive.fs.common.AngularVersion.NG;
import static supportive.fs.common.AngularVersion.TN_DEC;
import static supportive.reflectRefact.PsiWalkFunctions.*;

/**
 * factory for our various system contexts
 */
public class ContextFactory {

    IntellijFileContext project;

    protected ContextFactory(IntellijFileContext project) {
        this.project = project;
    }

    @Nullable
    public static PsiRouteContext createRouteContext(TypescriptFileContext routesFile, PsiElementContext psiElementContext, Class origin) {
        Optional<PsiElementContext> name = psiElementContext.queryContent(JS_PROPERTY, "NAME:(name)", PSI_ELEMENT_JS_STRING_LITERAL).reduce((el1, el2) -> el2);
        Optional<PsiElementContext> url = psiElementContext.queryContent(JS_PROPERTY, "NAME:(url)", PSI_ELEMENT_JS_STRING_LITERAL).reduce((el1, el2) -> el2);
        Optional<PsiElementContext> component = psiElementContext.queryContent(JS_PROPERTY, "NAME:(component)", PSI_ELEMENT_JS_IDENTIFIER).reduce((el1, el2) -> el2);
        String sName = "";
        String sUrl = "";
        String sComponent = "";
        boolean found = false;

        if (name.isPresent()) {
            sName = name.get().getText();
            found = true;
        }
        if (url.isPresent()) {
            sUrl = url.get().getText();
            found = true;
        }

        String sImport = "";
        if (component.isPresent()) {
            sComponent = component.get().getText();
            //now we try to find the include

            List<String> imports = routesFile.getImportIdentifiers(sComponent).stream().
                    flatMap(item -> item.queryContent(JS_ES_6_FROM_CLAUSE, PSI_ELEMENT_JS_STRING_LITERAL).map(fromImport -> fromImport.getText())).collect(Collectors.toList());

            sImport = imports.isEmpty() ? "" : imports.get(0);
            found = true;
        }

        //TODO component defined in the same file


        if (found) {
            return new PsiRouteContext(psiElementContext.getElement(), new Route(sName, sUrl, sComponent, psiElementContext.getName(), sImport, origin));
        }
        return null;
    }

    public static ContextFactory getInstance(IntellijFileContext project) {
        return new ContextFactory(project);
    }

    public List<IntellijFileContext> getProjects(AngularVersion angularVersion) {
        return AngularIndex.getAllAngularRoots(project.getProject(), angularVersion);
    }


    public List<IUIRoutesRoutesFileContext> getRouteFiles(IntellijFileContext projectRoot, AngularVersion angularVersion) {
        List<IUIRoutesRoutesFileContext> routeFiles = Lists.newLinkedList();

        if (angularVersion == NG) {
            routeFiles.addAll(NG_UIRoutesIndex.getAllMainRoutes(projectRoot.getProject(), projectRoot).stream()
                    .map(psiFile -> new NG_UIRoutesRoutesFileContext(projectRoot.getProject(), psiFile)).distinct().collect(Collectors.toList()));

            routeFiles.addAll(TNRoutesIndex.getAllMainRoutes(projectRoot.getProject(), projectRoot).stream()
                    .map(psiFile -> new TNAngularRoutesFileContext(projectRoot.getProject(), psiFile))
                    .distinct()
                    .collect(Collectors.toList()));
        } else {

            routeFiles.addAll(TN_UIRoutesIndex.getAllMainRoutes(project.getProject(), projectRoot).stream()
                    .map(psiFile -> new TNUIRoutesFileContext(projectRoot.getProject(), psiFile))
                    .distinct()
                    .collect(Collectors.toList()));
        }

        return routeFiles;

    }

    public List<IUIRoutesRoutesFileContext> getRouteFiles(IntellijFileContext projectRoot) {
        List<IUIRoutesRoutesFileContext> routeFiles = Lists.newLinkedList();

        routeFiles.addAll(getRouteFiles(projectRoot, TN_DEC));
        routeFiles.addAll(getRouteFiles(projectRoot, NG));

        return routeFiles;

    }

    @NotNull
    public List<NgModuleFileContext> getModules(IntellijFileContext projectRoot, AngularVersion angularVersion) {
        List<IntellijFileContext> angularRoots = AngularIndex.getAllAngularRoots(projectRoot.getProject(), angularVersion);
        return angularRoots.stream().flatMap(angularRoot -> ModuleIndex
                .getAllModuleFiles(projectRoot.getProject(), angularRoot).stream())
                .map(module -> new NgModuleFileContext(projectRoot.getProject(), module))
                .collect(Collectors.toList());
    }


    @NotNull
    public List<ComponentFileContext> getComponents(IntellijFileContext projectRoot, AngularVersion angularVersion) {
        List<IntellijFileContext> angularRoots = AngularIndex.getAllAngularRoots(projectRoot.getProject(), angularVersion);
        return angularRoots.stream().flatMap(angularRoot -> ComponentIndex
                .getAllComponentFiles(projectRoot.getProject(), angularRoot).stream())
                .map(component -> new ComponentFileContext(projectRoot.getProject(), component))
                .collect(Collectors.toList());
    }

    public ResourceFilesContext getProjectResources(IntellijFileContext projectRoot) {
        ResourceFilesContext resourceFilesContext = new ResourceFilesContext(projectRoot.getProject());


        resourceFilesContext.getRoutes().addAll(getRouteFiles(projectRoot));

        List<NgModuleFileContext> modulesTn = getModules(projectRoot, TN_DEC);
        List<NgModuleFileContext> modulesNg = getModules(projectRoot, NG);

        List<ComponentFileContext> componentsTn = getComponents(projectRoot, TN_DEC);
        List<ComponentFileContext> componentsNg = getComponents(projectRoot, NG);

        resourceFilesContext.getModules().addAll(modulesTn);
        resourceFilesContext.getComponents().addAll(componentsTn);
        return resourceFilesContext;
    }

    public List<NgModuleFileContext> getModulesNg(Optional<IntellijFileContext> moduleElement) {
        throw new RuntimeException("Not implemented yet");
    }


}
