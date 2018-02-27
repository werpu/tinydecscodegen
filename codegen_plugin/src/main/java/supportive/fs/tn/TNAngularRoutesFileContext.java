package supportive.fs.tn;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import supportive.fs.common.*;
import supportive.refactor.DummyInsertPsiElement;
import supportive.refactor.RefactorUnit;
import supportive.reflectRefact.PsiWalkFunctions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Integer.valueOf;
import static supportive.reflectRefact.PsiWalkFunctions.*;
import static supportive.utils.StringUtils.elVis;
import static supportive.utils.StringUtils.literalEquals;

/**
 * helper context to deal with routes and subroutes
 * in a single file using the tiny decorations
 */
public class TNAngularRoutesFileContext extends TNRoutesFileContext {

    public TNAngularRoutesFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
        init();
    }

    public TNAngularRoutesFileContext(AnActionEvent event) {
        super(event);
        init();
    }

    public TNAngularRoutesFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
        init();
    }

    public TNAngularRoutesFileContext(IntellijFileContext fileContext) {
        super(fileContext);
        init();
    }


    protected void init() {

        constructors = this.queryContent(">" + TYPE_SCRIPT_FUNC, p_isConstructor(), p_isRouteProviderPresent()
        ).collect(Collectors.toList());
    }


    @Override
    public void addRoute(Route routeData) {
        //find route provider inject
        routeData.setComponent(super.appendImport(routeData.getComponent(), routeData.getComponentPath()));

        for (PsiElementContext constructor : constructors) {
            String routeProviderName = getStateOrRouteProviderName(constructor);


            Optional<PsiElementContext> body = constructor.findPsiElement(PsiWalkFunctions::isJSBlock);
            int insertPos = calculateRouteInsertPos(routeProviderName, constructor, body);


            addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(insertPos), toRoute(routeProviderName, routeData)));
        }
    }


    public int calculateRouteInsertPos(String routeProviderName, PsiElementContext constructor, Optional<PsiElementContext> body) {
        //now append the route to the contsructor after the last routprovider declaration or to the bottom
        List<PsiElementContext> whenCallArgs = getRouteParams(constructor, routeProviderName);
        int insertPos = 0;
        if (whenCallArgs.size() > 0) {
            PsiElementContext elementContext = whenCallArgs.get(whenCallArgs.size() - 1);
            insertPos = elementContext.getElement().getStartOffsetInParent() + elementContext.getElement().getTextLength();
        } else {
            insertPos = body.get().getTextOffset() + 1;
        }
        return insertPos;
    }

    List<PsiElementContext> getRouteParams(PsiElementContext constructor, String routeProviderName) {

        return constructor
                .queryContent(PSI_ELEMENT_JS_IDENTIFIER, "TEXT:('when')")
                .map(item -> item.walkParent(el -> {
                    return literalEquals(el.toString(), JS_EXPRESSION_STATEMENT);
                }))
                .filter(item -> item.isPresent())
                .flatMap(item -> item.get().queryContent(JS_ARGUMENTS_LIST))
                .filter(item -> {
                    Optional<PsiElement> prev = elVis(item, "element", "prevSibling");
                    return prev.isPresent() && prev.get().getText().endsWith(".when");
                })
                .collect(Collectors.toList());

    }

    /**
     * central method which fetches the routes
     * @return
     */
    @Override
    public List<PsiRouteContext> getRoutes() {
        boolean routeProvider = constructors.stream().filter(p_isRouteProviderPresent()).findFirst().isPresent();

        if (routeProvider) {
            return mapRoutes();
        }
        return Collections.emptyList();
    }

    private List<PsiRouteContext> mapRoutes() {
        PsiElementContext constr = constructors.stream().filter(p_isRouteProviderPresent()).findFirst().get();
        String routeProviderName = getStateOrRouteProviderName(constr);
        List<PsiElementContext> whenParams = getRouteParams(constr, routeProviderName);

        return whenParams.stream()
                .distinct()
                .flatMap(resolveArgs())
                .filter(item -> item.isPresent())
                .map(item -> item.get())
                .collect(Collectors.toList());
    }


}
