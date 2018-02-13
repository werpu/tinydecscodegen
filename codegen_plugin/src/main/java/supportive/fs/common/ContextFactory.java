package supportive.fs.common;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiFile;
import indexes.AngularIndex;
import indexes.RoutesIndex;
import indexes.TNRoutesIndex;
import org.jetbrains.annotations.Nullable;
import supportive.fs.ng.UIRoutesRoutesFileContext;
import supportive.fs.tn.TNUIRoutesRoutesFileContext;

import javax.naming.OperationNotSupportedException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public static PsiRouteContext createRouteContext(TypescriptFileContext routesFile, PsiElementContext psiElementContext) {
        Optional<PsiElementContext> name = psiElementContext.queryContent(JS_PROPERTY,  "NAME:(name)",PSI_ELEMENT_JS_STRING_LITERAL).reduce((el1, el2) -> el2);
        Optional<PsiElementContext> url = psiElementContext.queryContent(JS_PROPERTY, "NAME:(url)",PSI_ELEMENT_JS_STRING_LITERAL).reduce((el1, el2) -> el2);
        Optional<PsiElementContext> component = psiElementContext.queryContent(JS_PROPERTY, "NAME:(component)",PSI_ELEMENT_JS_IDENTIFIER).reduce((el1, el2) -> el2);
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
            return new PsiRouteContext(psiElementContext.getElement(), new Route(sName, sUrl, sComponent, psiElementContext.getName(), sImport));
        }
        return null;
    }

    public static ContextFactory getInstance(IntellijFileContext project) {
        return new ContextFactory(project);
    }

    public List<IntellijFileContext> getProjects(AngularVersion angularVersion) {
        return  AngularIndex.getAllAngularRoots(project.getProject(), angularVersion);
    }


    public List<IUIRoutesRoutesFileContext> getRouteFiles(IntellijFileContext projectRoot) {
        List<IUIRoutesRoutesFileContext> routeFiles = Lists.newLinkedList();

        routeFiles.addAll(RoutesIndex.getAllMainRoutes(projectRoot.getProject(), projectRoot).stream()
                .map(psiFile -> new UIRoutesRoutesFileContext(projectRoot.getProject(), psiFile)).collect(Collectors.toList()));

        routeFiles.addAll( TNRoutesIndex.getAllMainRoutes(projectRoot.getProject(), projectRoot).stream()
                .map(psiFile -> new TNUIRoutesRoutesFileContext(projectRoot.getProject(), psiFile))
                .collect(Collectors.toList()));
        return routeFiles;

    }
    public List<NgModuleFileContext> getModulesNg(Optional<IntellijFileContext> moduleElement) {
        throw new RuntimeException("Not implemented yet");
    }



}