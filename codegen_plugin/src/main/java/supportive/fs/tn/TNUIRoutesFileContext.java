package supportive.fs.tn;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import supportive.fs.common.IntellijFileContext;
import supportive.fs.common.PsiElementContext;
import supportive.fs.common.PsiRouteContext;
import supportive.fs.common.Route;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static supportive.reflectRefact.PsiWalkFunctions.*;
import static supportive.utils.StringUtils.elVis;
import static supportive.utils.StringUtils.literalEquals;

public class TNUIRoutesFileContext extends TNRoutesFileContext {

    public TNUIRoutesFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
        init();
    }

    public TNUIRoutesFileContext(AnActionEvent event) {
        super(event);
        init();
    }

    public TNUIRoutesFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
        init();
    }

    public TNUIRoutesFileContext(IntellijFileContext fileContext) {
        super(fileContext);
        init();
    }


    protected void init() {

        constructors = this.queryContent(">" + TYPE_SCRIPT_FUNC, p_isConstructor(), p_isStateProviderPresent()
        ).collect(Collectors.toList());
    }

    @Override
    public void addRoute(Route routeData) {

    }


    @Override
    public List<PsiRouteContext> getRoutes() {
        boolean stateProvider = constructors.stream().filter(p_isStateProviderPresent()).findFirst().isPresent();



        if (stateProvider) {
            return mapStates();
        }
        return Collections.emptyList();
    }


    private List<PsiRouteContext> mapStates() {
        PsiElementContext constr = constructors.stream().filter(p_isRouteProviderPresent()).findFirst().get();
        String stateProviderName = getStateOrRouteProviderName(constr);
        List<PsiElementContext> stateParams = getRouteParams(constr, stateProviderName);

        return stateParams.stream()
                .distinct()
                .flatMap(resolveArgs())
                .filter(item -> item.isPresent())
                .map(item -> item.get())
                .collect(Collectors.toList());
    }

    @Override
    List<PsiElementContext> getRouteParams(PsiElementContext constructor, String routeProviderName) {

        return constructor
                .queryContent(PSI_ELEMENT_JS_IDENTIFIER, "TEXT:('state')")
                .map(item -> item.walkParent(el -> {
                    return literalEquals(el.toString(), JS_EXPRESSION_STATEMENT);
                }))
                .filter(item -> item.isPresent())
                .flatMap(item -> item.get().queryContent(JS_ARGUMENTS_LIST))
                .filter(item -> {
                    Optional<PsiElement> prev = elVis(item, "element", "prevSibling");
                    return prev.isPresent() && prev.get().getText().endsWith(".state");
                })
                .collect(Collectors.toList());

    }


}
