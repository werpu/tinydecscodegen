package net.werpu.tools.supportive.fs.ng;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.werpu.tools.supportive.fs.common.*;
import net.werpu.tools.supportive.refactor.DummyInsertPsiElement;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.utils.IntellijUtils.getTsExtension;
import static net.werpu.tools.supportive.utils.StringUtils.*;

/**
 * helper context to deal with routes and subroutes
 * in a single file
 */
public class NG_UIRoutesRoutesFileContext extends TypescriptFileContext implements IUIRoutesRoutesFileContext {


    PsiElementContext routesArr;


    public NG_UIRoutesRoutesFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public NG_UIRoutesRoutesFileContext(IntellijFileContext fileContext) {
        super(fileContext);
    }


    /**
     * adds a new route to the existing file
     * TODO also add the include if possible
     *
     * @param routeData
     */
    @Override
    public void addRoute(Route routeData) {


        routeData.setComponent(appendImport(routeData.getComponent(), routeData.getComponentPath()));


        int cnt = 1;
        String origUrl = routeData.getUrl();

        while (isUrlInUse(routeData)) {
            routeData.setUrl(origUrl + "_" + cnt);
            cnt++;
        }


        addRefactoring(new RefactorUnit(super.getPsiFile(), new DummyInsertPsiElement(getRoutesDeclaration().get().getRootElement().getElement().getTextOffset()), routeData.toStringNg2UIRoutes()));
        addNavVar(routeData.getRouteVarName());
    }

    public void addRoute(Route routeData, NgModuleFileContext module) {


        routeData.setComponent(appendImport(routeData.getComponent(), routeData.getComponentPath()));


        int cnt = 1;
        String origUrl = routeData.getUrl();

        while (isUrlInUse(routeData)) {
            routeData.setUrl(origUrl + "_" + cnt);
            cnt++;
        }


        addRefactoring(new RefactorUnit(super.getPsiFile(), new DummyInsertPsiElement(getRoutesDeclaration().get().getRootElement().getElement().getTextOffset()), routeData.toStringNg2UIRoutes()));
        addRefactoring(new RefactorUnit(super.getPsiFile(), new DummyInsertPsiElement(getRoutesDeclaration().get().getRootElement().getElement().getTextOffset()), routeData.toLocalRoutes()));

        try {
            module.appendImports(routeData.getLocalRouteDeclName());
            Path routeFilePath = Paths.get(this.getVirtualFile().getPath());
            Path moduleFilePath = Paths.get(module.getFolderPath());
            String rel = normalizePath(moduleFilePath.relativize(routeFilePath).toString());
            if(rel.endsWith(getTsExtension())) {
                rel = rel.substring(0, rel.length() - getTsExtension().length());
            }

            module.appendImport(routeData.getLocalRouteDeclName(), rel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNavVar(NgModuleFileContext target, Route routeData) {

    }

    @Override
    public boolean isUrlInUse(Route routeData) {
        return urlCheck(routeData, getPsiFile().getText());
    }

    @Override
    public boolean isRouteVarNameUsed(Route routeData) {
        return getNavigationalArray().get().$q(PSI_ELEMENT_JS_IDENTIFIER, TreeQueryEngine.EL_TEXT_EQ(routeData.getRouteVarName())).findAny().isPresent();
    }

    @Override
    public boolean isRouteNameUsed(Route routeData) {
        return getPsiFile().getText().contains("name: '" + routeData.getRouteKey() + "'");
    }


    public boolean urlCheck(Route routeData, String fullText) {
        return literalContains(fullText, routeData.getUrl());
    }

    @NotNull
    public Optional<PsiElementContext> getRoutesDeclaration() {

        List<PsiElement> els = findPsiElements(PsiWalkFunctions::isRootNav);
        return els.stream().map(el -> new PsiElementContext(el)).findFirst();
    }


    @NotNull
    public Optional<PsiElementContext> getNavigationalArray() {

        List<PsiElement> els = findPsiElements(PsiWalkFunctions::isRootNav);
        return els.stream().map(el -> new PsiElementContext(el))
                .flatMap(elc -> elc.findPsiElements(el -> el.toString().startsWith(PsiWalkFunctions.JS_PROPERTY) && elVis(el, "nameIdentifier", "text").isPresent() && elVis(el, "nameIdentifier", "text").get().equals("states")).stream())
                .map(elc -> elc.findPsiElement(el -> el.toString().startsWith(PsiWalkFunctions.JS_ARRAY_LITERAL_EXPRESSION))).findFirst().get();
    }

    public void addNavVar(String varName) {
        Optional<PsiElementContext> closingBracket = getEndOfNavArr();
        if (closingBracket.isPresent()) {
            addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(closingBracket.get().getElement().getTextOffset()), ", " + varName));
        }
    }

    @Nullable
    public Optional<PsiElementContext> getEndOfNavArr() {
        PsiElementContext navArr = getNavigationalArray().get();

        //find the last closing bracket
        return Optional.ofNullable(navArr
                .findPsiElements(el -> el.toString().equals(PsiWalkFunctions.PSI_ELEMENT_JS_RBRACKET)).stream() //TODO type check once debugged out
                .reduce((first, second) -> second).orElse(null));
    }


    /**
     * gets the flat list of routes in this context
     *
     * @return
     */
    public List<PsiRouteContext> getRoutes() {

        //<TODO subroutes>
        //nav array not proper iot cannot handle subroutes
        List<PsiElementContext> foundIdentifiers =
                concat(this.$q(TYPE_SCRIPT_VARIABLE, JS_OBJECT_LITERAL_EXPRESSION, JS_PROPERTY, TreeQueryEngine.EL_NAME_EQ("views"), TreeQueryEngine.PARENTS_EQ(TYPE_SCRIPT_VARIABLE)),
                        this.$q(TYPE_SCRIPT_VARIABLE,JS_OBJECT_LITERAL_EXPRESSION,  JS_PROPERTY, TreeQueryEngine.EL_NAME_EQ("component"), TreeQueryEngine.PARENTS_EQ(TYPE_SCRIPT_VARIABLE)))
                .distinct()
               .collect(Collectors.toList());


       // List<PsiElementContext> foundIdentifiers = getNavigationalArray().get().queryContent(JS_REFERENCE_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER).collect(Collectors.toList());
        //List<PsiElementContext> foundParseableBlocks = getNavigationalArray().get().queryContent(JS_OBJECT_LITERAL_EXPRESSION).collect(Collectors.toList());

        //part a we try to resolve the variables locally
        List<PsiRouteContext> retVal = Lists.newArrayList();


        retVal.addAll(foundIdentifiers.stream()
                .map(psiElementContext -> ContextFactory.createRouteContext(this, psiElementContext, this.getClass())).filter(found -> found != null)
                .collect(Collectors.toList()));

        /*retVal.addAll(foundParseableBlocks.stream()
                .map(psiElementContext -> ContextFactory.createRouteContext(this, psiElementContext, this.getClass())).filter(found -> found != null)
                .collect(Collectors.toList()));*/

        return retVal;

    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    //only one route el per file allowed atm
}
