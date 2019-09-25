/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.supportive.fs.tn;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.PsiRouteContext;
import net.werpu.tools.supportive.fs.common.Route;
import net.werpu.tools.supportive.refactor.DummyInsertPsiElement;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.writeTransaction;
import static net.werpu.tools.supportive.utils.StringUtils.elVis;
import static net.werpu.tools.supportive.utils.StringUtils.literalEquals;

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

        constructors = this.queryContent(TYPE_SCRIPT_FUNC, p_isConstructor(), p_isRouteProviderPresent()
        ).collect(Collectors.toList());

    }

    @Override
    public void addRoute(Route routeData) {
        //find route provider inject

        writeTransaction(getProject(), () -> {
            routeData.setComponent(super.appendImport(routeData.getComponent().replaceAll("\\[.*\\]+", ""), routeData.getComponentPath()));

            for (PsiElementContext constructor : constructors) {
                String routeProviderName = getStateOrRouteProviderName(constructor);

                Optional<PsiElementContext> body = constructor.$q(JS_BLOCK_STATEMENT).findFirst();
                int insertPos = calculateRouteInsertPos(routeProviderName, constructor, body);

                addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(insertPos), toRoute(routeProviderName, routeData)));
            }
        });

    }

    public int calculateRouteInsertPos(String routeProviderName, PsiElementContext constructor, Optional<PsiElementContext> body) {
        //now append the route to the contsructor after the last routprovider declaration or to the bottom
        List<PsiElementContext> whenCallArgs = getRouteParams(constructor, routeProviderName);
        int insertPos = 0;
        if (whenCallArgs.size() > 0) {
            PsiElementContext elementContext = whenCallArgs.get(whenCallArgs.size() - 1);
            insertPos = elementContext.getElement().getTextOffset() + elementContext.getTextLength() + 1;
        } else {
            insertPos = body.get().getTextRangeOffset() + 1;
        }
        return insertPos;
    }

    List<PsiElementContext> getRouteParams(PsiElementContext constructor, String routeProviderName) {

        return constructor
                .queryContent(PSI_ELEMENT_JS_IDENTIFIER, TreeQueryEngine.TEXT_EQ("'when'"))
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
     *
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
