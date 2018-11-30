package net.werpu.tools.supportive.fs.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import net.werpu.tools.indexes.*;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.werpu.tools.supportive.fs.common.errors.ResourceClassNotFound;
import net.werpu.tools.supportive.fs.ng.NG_UIRoutesRoutesFileContext;
import net.werpu.tools.supportive.fs.tn.TNAngularRoutesFileContext;
import net.werpu.tools.supportive.fs.tn.TNUIRoutesFileContext;
import net.werpu.tools.supportive.utils.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.werpu.tools.supportive.fs.common.AngularVersion.NG;
import static net.werpu.tools.supportive.fs.common.AngularVersion.TN_DEC;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.PARENT_SEARCH;

/**
 * factory for our various system contexts
 */
public class ContextFactory {

    IntellijFileContext project;

    private static Cache<String, ResourceFilesContext> volatileData = CacheBuilder.newBuilder()
            .build();


    protected ContextFactory(IntellijFileContext project) {
        this.project = project;
    }

    @Nullable
    public static PsiRouteContext createRouteContext(TypescriptFileContext routesFile, PsiElementContext psiElementContext, Class origin) {
        Optional<PsiElementContext> name = psiElementContext.queryContent(JS_PROPERTY, TreeQueryEngine.NAME_EQ("name"), PSI_ELEMENT_JS_STRING_LITERAL).reduce((el1, el2) -> el2);
        Optional<PsiElementContext> url = psiElementContext.queryContent(JS_PROPERTY,  TreeQueryEngine.NAME_EQ("url"), PSI_ELEMENT_JS_STRING_LITERAL).reduce((el1, el2) -> el2);
        Optional<PsiElementContext> component = psiElementContext.queryContent(PSI_ELEMENT_JS_IDENTIFIER, TreeQueryEngine.TEXT_EQ("component"), TreeQueryEngine.PARENTS_EQ(JS_PROPERTY), JS_REFERENCE_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER).findFirst();
        String sName = "";
        String sUrl = "";
        String sComponent = "";
        boolean found = false;

        if (name.isPresent()) {
            sName = name.get().getUnquotedText();
            found = true;
        }
        if (url.isPresent()) {
            sUrl = url.get().getUnquotedText();
            found = true;
        }


        String sImport = "";
        if (component.isPresent()) {
            sComponent = component.get().getUnquotedText();
            //now we try to find the include


            List<String> imports = routesFile.getImportIdentifiers(sComponent).stream().
                    flatMap(item -> item.queryContent(PARENT_SEARCH(ANY_TS_IMPORT), JS_ES_6_FROM_CLAUSE, PSI_ELEMENT_JS_STRING_LITERAL).map(fromImport -> fromImport.getText())).collect(Collectors.toList());


            sImport = imports.isEmpty() ? "" : imports.get(0);
            found = true;
        }

        //TODO component defined in the same file

        final String modulePath = StringUtils.normalizePath(routesFile.getFolderPath());

        if (found) {
            return new PsiRouteContext(psiElementContext.getElement(), new Route(sName, sUrl, sComponent, psiElementContext.getName(), modulePath+"/"+sImport, origin));
        }
        return null;
    }

    public static ContextFactory getInstance(IntellijFileContext project) {
        return new ContextFactory(project);
    }

    public List<IntellijFileContext> getProjects(AngularVersion angularVersion) {
        return AngularIndex.getAllAffectedRoots(project.getProject(), angularVersion);
    }


    public List<IUIRoutesRoutesFileContext> getRouteFiles(IntellijFileContext projectRoot, AngularVersion angularVersion) {
        List<IUIRoutesRoutesFileContext> routeFiles = Lists.newLinkedList();

        if (angularVersion == NG) {
            routeFiles.addAll(NG_UIRoutesIndex.getAllAffectedFiles(projectRoot.getProject(), projectRoot).stream()
                    .map(psiFile -> new NG_UIRoutesRoutesFileContext(projectRoot.getProject(), psiFile))
                    .distinct().collect(Collectors.toList()));

            routeFiles.addAll(TNRoutesIndex.getAllAffectedFiles(projectRoot.getProject(), projectRoot).stream()
                    .map(psiFile -> new TNAngularRoutesFileContext(projectRoot.getProject(), psiFile))
                    .distinct()
                    .collect(Collectors.toList()));
        } else {

            routeFiles.addAll(TN_UIRoutesIndex.getAllAffectedFiles(project.getProject(), projectRoot).stream()
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
        List<IntellijFileContext> angularRoots = AngularIndex.getAllAffectedRoots(projectRoot.getProject(), angularVersion);
        return angularRoots.stream().flatMap(angularRoot -> ModuleIndex
                .getAllAffectedFiles(projectRoot.getProject(), angularRoot).stream())
                .filter(file -> file.getVirtualFile().exists())
                .map(module -> {
                    try {
                        return new NgModuleFileContext(projectRoot.getProject(), module);
                    } catch (ResourceClassNotFound ex) { //broken modules, old module style without ngclass etc
                        return null;
                    }
                })
                .filter(e -> e != null)
                .collect(Collectors.toList());
    }


    @NotNull
    public List<NgModuleFileContext> getModulesFor(IntellijFileContext projectRoot, AngularVersion angularVersion, String filterPath) {
        List<IntellijFileContext> angularRoots = AngularIndex.getAllAffectedRoots(projectRoot.getProject(), angularVersion);
        return angularRoots.stream().flatMap(angularRoot -> ModuleIndex
                .getAllAffectedFiles(projectRoot.getProject(), angularRoot).stream())
                .filter(file -> file.getVirtualFile().exists())
                .map(module -> {
                    try {
                        return new NgModuleFileContext(projectRoot.getProject(), module);
                    } catch (Throwable e) {
                        return null;
                    }
                })
                .filter(e -> e != null)
                .filter(ngModuleFileContext -> {
                    String mPath = StringUtils.normalizePath(ngModuleFileContext.getPsiFile().getParent().getVirtualFile().getPath());
                    String filter = StringUtils.normalizePath(filterPath);

                    return filter.toLowerCase().contains(mPath.toLowerCase());

                })
                .collect(Collectors.toList());
    }


    @NotNull
    public List<ComponentFileContext> getComponents(IntellijFileContext projectRoot, AngularVersion angularVersion) {
        List<IntellijFileContext> angularRoots = AngularIndex.getAllAffectedRoots(projectRoot.getProject(), angularVersion);
        return angularRoots.stream().flatMap(angularRoot -> ComponentIndex
                .getAllAffectedFiles(projectRoot.getProject(), angularRoot).stream())
                .filter(file -> file.getVirtualFile().exists())
                .map(component -> {
                    try {
                        return new ComponentFileContext(projectRoot.getProject(), component);
                    } catch (Throwable e) {
                        return null;
                    }
                })
                .filter(e -> e != null)
                .collect(Collectors.toList());
    }

    @NotNull
    public List<ServiceContext> getServices(IntellijFileContext projectRoot, AngularVersion angularVersion) {
        List<IntellijFileContext> angularRoots = AngularIndex.getAllAffectedRoots(projectRoot.getProject(), angularVersion);
        return angularRoots.stream().flatMap(angularRoot -> ServiceIndex
                .getAllAffectedFiles(projectRoot.getProject(), angularRoot).stream())
                .filter(file -> file.getVirtualFile().exists())
                .map(service -> {
                    try {
                        return new ServiceContext(projectRoot.getProject(), service, service.getOriginalElement());
                    } catch (Throwable e) {
                        return null;
                    }
                })
                .filter(e -> e != null)
                .collect(Collectors.toList());
    }

    @NotNull
    public List<ComponentFileContext> getController(IntellijFileContext projectRoot, AngularVersion angularVersion) {
        List<IntellijFileContext> angularRoots = AngularIndex.getAllAffectedRoots(projectRoot.getProject(), angularVersion);
        return angularRoots.stream().flatMap(angularRoot -> ControllerIndex
                .getAllAffectedFiles(projectRoot.getProject(), angularRoot).stream())
                .filter(file -> file.getVirtualFile().exists())
                .map(controller -> {
                    try {
                        return new ComponentFileContext(projectRoot.getProject(), controller);
                    } catch (Throwable e) {
                        return null;
                    }
                })
                .filter(e -> e != null)
                .collect(Collectors.toList());
    }

    @NotNull
    public List<FilterPipeContext> getFilters(IntellijFileContext projectRoot, AngularVersion angularVersion) {
        List<IntellijFileContext> angularRoots = AngularIndex.getAllAffectedRoots(projectRoot.getProject(), angularVersion);
        return angularRoots.stream().flatMap(angularRoot -> FilterIndex
                .getAllAffectedFiles(projectRoot.getProject(), angularRoot).stream())
                .filter(file -> file.getVirtualFile().exists())
                .map(filters -> {
                    try {
                        return new FilterPipeContext(projectRoot.getProject(), filters, filters.getOriginalElement());
                    } catch (Throwable e) {
                        return null;
                    }
                })
                .filter(e -> e != null)

                .collect(Collectors.toList());
    }

    public ResourceFilesContext getProjectResourcesCached(IntellijFileContext projectRoot, AngularVersion angularVersion) {
        ResourceFilesContext retVal = volatileData.getIfPresent(angularVersion.name());
        if(retVal != null) {
            return  retVal;
        }
        return getProjectResources(projectRoot, angularVersion);

    }

    public ResourceFilesContext getProjectResources(IntellijFileContext projectRoot, AngularVersion angularVersion) {
        ResourceFilesContext resourceFilesContext = new ResourceFilesContext(projectRoot.getProject());


        resourceFilesContext.getRoutes().addAll(getRouteFiles(projectRoot, angularVersion));



        List<NgModuleFileContext> modulesTn = getModules(projectRoot, angularVersion);
        List<ComponentFileContext> componentsTn = getComponents(projectRoot, angularVersion);
        List<ComponentFileContext> controllersTn = getController(projectRoot, angularVersion);
        List<ServiceContext> service = getServices(projectRoot, angularVersion);
        List<FilterPipeContext> filters = getFilters(projectRoot, angularVersion);


        resourceFilesContext.getModules().addAll(modulesTn);
        resourceFilesContext.getComponents().addAll(componentsTn);
        resourceFilesContext.getServices().addAll(service);
        resourceFilesContext.getControllers().addAll(controllersTn);
        resourceFilesContext.getFiltersPipes().addAll(filters);


        volatileData.put(angularVersion.name(), resourceFilesContext);

        return resourceFilesContext;
    }


}
