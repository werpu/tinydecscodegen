package supportive.fs.common;

import com.google.common.collect.Lists;
import indexes.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.ng.NG_UIRoutesRoutesFileContext;
import supportive.fs.tn.TNAngularRoutesFileContext;
import supportive.fs.tn.TNUIRoutesFileContext;
import supportive.utils.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static supportive.fs.common.AngularVersion.NG;
import static supportive.fs.common.AngularVersion.TN_DEC;
import static supportive.reflectRefact.PsiWalkFunctions.*;
import static supportive.utils.IntellijRunUtils.newFutureRoTask;

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
        return AngularIndex.getAllAffectedRoots(project.getProject(), angularVersion);
    }


    public List<IUIRoutesRoutesFileContext> getRouteFiles(IntellijFileContext projectRoot, AngularVersion angularVersion) {
        List<IUIRoutesRoutesFileContext> routeFiles = Lists.newLinkedList();

        if (angularVersion == NG) {
            routeFiles.addAll(NG_UIRoutesIndex.getAllAffectedFiles(projectRoot.getProject(), projectRoot).stream()
                    .map(psiFile -> new NG_UIRoutesRoutesFileContext(projectRoot.getProject(), psiFile)).distinct().collect(Collectors.toList()));

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
                .map(module -> new NgModuleFileContext(projectRoot.getProject(), module))
                .collect(Collectors.toList());
    }


    @NotNull
    public List<NgModuleFileContext> getModulesFor(IntellijFileContext projectRoot, AngularVersion angularVersion, String filterPath) {
        List<IntellijFileContext> angularRoots = AngularIndex.getAllAffectedRoots(projectRoot.getProject(), angularVersion);
        return angularRoots.stream().flatMap(angularRoot -> ModuleIndex
                .getAllAffectedFiles(projectRoot.getProject(), angularRoot).stream())

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

    public ResourceFilesContext getProjectResources(IntellijFileContext projectRoot, AngularVersion angularVersion) {
        ResourceFilesContext resourceFilesContext = new ResourceFilesContext(projectRoot.getProject());


        resourceFilesContext.getRoutes().addAll(getRouteFiles(projectRoot, angularVersion));

        ExecutorService executor = Executors.newCachedThreadPool();

        FutureTask<List<NgModuleFileContext>> fModulesTn = newFutureRoTask(() -> getModules(projectRoot, angularVersion));
        FutureTask<List<ComponentFileContext>> fComp = newFutureRoTask(() -> getComponents(projectRoot, angularVersion));
        FutureTask<List<ComponentFileContext>> fCtrl = newFutureRoTask(() -> getController(projectRoot, angularVersion));
        FutureTask<List<ServiceContext>> fservice = newFutureRoTask(() -> getServices(projectRoot, angularVersion));
        FutureTask<List<FilterPipeContext>> fFilters = newFutureRoTask(() -> getFilters(projectRoot, angularVersion));

        executor.execute(fModulesTn);
        executor.execute(fComp);
        executor.execute(fCtrl);
        executor.execute(fservice);
        executor.execute(fFilters);

        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        /*List<NgModuleFileContext> modulesTn = getModules(projectRoot, angularVersion);
        List<ComponentFileContext> componentsTn = getComponents(projectRoot, angularVersion);
        List<ComponentFileContext> controllersTn = getController(projectRoot, angularVersion);
        List<ServiceContext> service = getServices(projectRoot, angularVersion);
        List<FilterPipeContext> filters = getFilters(projectRoot, angularVersion);*/

        try {
            resourceFilesContext.getModules().addAll(fModulesTn.get());
            resourceFilesContext.getComponents().addAll(fComp.get());
            resourceFilesContext.getServices().addAll(fservice.get());
            resourceFilesContext.getControllers().addAll(fCtrl.get());
            resourceFilesContext.getFiltersPipes().addAll(fFilters.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return resourceFilesContext;
    }



}
