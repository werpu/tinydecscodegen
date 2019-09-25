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

package net.werpu.tools.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.psi.PsiFile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.actions_all.shared.ComponentSelectorModel;
import net.werpu.tools.actions_all.shared.Messages;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.gui.CreateRoute;
import net.werpu.tools.gui.support.InputDialogWrapperBuilder;
import net.werpu.tools.indexes.ControllerIndex;
import net.werpu.tools.indexes.TNRoutesIndex;
import net.werpu.tools.indexes.TN_UIRoutesIndex;
import net.werpu.tools.supportive.fs.common.ComponentFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.Route;
import net.werpu.tools.supportive.fs.tn.TNAngularRoutesFileContext;
import net.werpu.tools.supportive.fs.tn.TNRoutesFileContext;
import net.werpu.tools.supportive.fs.tn.TNUIRoutesFileContext;
import net.werpu.tools.supportive.utils.StringUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static net.werpu.tools.actions_all.shared.FormAssertions.*;

public class CreateTnDecRoute extends AnAction {
    //TODO element nearest
    public static List<ComponentFileContext> getControllers(IntellijFileContext ctx) {
        return ComponentFileContext.getControllerInstances(ctx);
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.tnVisible(anActionEvent);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        IntellijFileContext fileContext = new IntellijFileContext(event);
        final net.werpu.tools.gui.CreateRoute mainForm = new net.werpu.tools.gui.CreateRoute();
        ComponentSelectorModel selectorModel = null;

        //this is only for angular ng, angular 1 does not have a module isolation system
        mainForm.getLblRegisterIntoModule().setVisible(false);
        mainForm.getCbRegisterIntoModule().setVisible(false);

        DialogWrapper dialogWrapper = new InputDialogWrapperBuilder(fileContext.getProject(), mainForm.getRootPanel())
                .withDimensionKey("AnnRoute").withValidator(() -> {
                    Route route = getRoute(mainForm);
                    return getRoutesFiles(fileContext).flatMap(el -> CreateTnDecRoute.this.validate(route, el, mainForm).stream()).collect(Collectors.toList());
                }).create();

        ComponentFileContext[] components = findAllPageComponents(fileContext);
        if (components.length == 0) {

            String message = "There was no component found, cannot create route automatically, please create it manually";
            net.werpu.tools.supportive.utils.IntellijUtils.showErrorDialog(fileContext.getProject(), "Error", message);
            return;
        }

        List<ComponentFileContext> editorControllers = (fileContext.getVirtualFile().isDirectory()) ? Collections.emptyList() : getControllers(fileContext);

        selectorModel = new ComponentSelectorModel(components);
        List<String> selectorIndex = Arrays.asList(selectorModel.getContextNames());
        mainForm.getCbComponent().setModel(new ListComboBoxModel(selectorIndex));
        Optional<ComponentFileContext> defaultComponentData = getDefaultComponentData(fileContext);
        if (editorControllers.size() > 0 || defaultComponentData.isPresent()) {
            ComponentFileContext cData = defaultComponentData.get();
            mainForm.getCbComponent().setSelectedIndex(selectorIndex.indexOf(cData.getDisplayName()));
            String componentClassName = cData.getClazzName();
            mainForm.getCbComponent().setSelectedItem(componentClassName);
            mainForm.getTxtRouteName().setText(StringUtils.toLowerDash(componentClassName).replaceAll("_component", ""));
        } else {
            mainForm.updateRouteName(null);
        }

        dialogWrapper.setTitle("Create Route");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));

        dialogWrapper.show();

        if (dialogWrapper.isOK()) {
            Route route = getRoute(mainForm);
            ComponentFileContext compContext = components[mainForm.getCbComponent().getSelectedIndex()];

            WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {
                try {
                    getRoutesFiles(fileContext)
                            .forEach(rContext -> {
                                //calculate the component include relative from the file
                                try {
                                    Route myRoute = route.clone();
                                    myRoute.setComponentPath(compContext.calculateRelPathTo(rContext));
                                    myRoute.setComponent(myRoute.getComponent().replaceAll("\\[[^\\]]+\\]", ""));
                                    rContext.addRoute(myRoute);

                                    rContext.commit();
                                    rContext.reformat();

                                } catch (IOException | CloneNotSupportedException e) {
                                    throw new RuntimeException(e);
                                }

                            });
                    net.werpu.tools.supportive.utils.IntellijUtils.showInfoMessage("The new route has been added", "Info");
                } catch (RuntimeException e) {
                    net.werpu.tools.supportive.utils.IntellijUtils.showErrorDialog(fileContext.getProject(), "Error", e.getMessage());
                    e.printStackTrace();
                }

            });
        }
    }

    //dedup this code
    protected List<ValidationInfo> validate(Route route, TNRoutesFileContext ctx, CreateRoute mainForm) {
        return Arrays.asList(
                assertNotNullOrEmpty(mainForm.getTxtRouteName().getText(), Messages.ERR_NAME_VALUE, mainForm.getTxtRouteName()),
                assertPattern(mainForm.getTxtRouteName().getText(), VALID_ROUTE, Messages.ERR_CONFIG_PATTERN, mainForm.getTxtRouteName()),
                assertTrue(!ctx.isRouteNameUsed(route), "The route name already is in use", mainForm.getTxtRouteName()),
                assertTrue(!ctx.isUrlInUse(route), "The route url already is in use", mainForm.getTxtHref())
        ).stream().filter(s -> s != null).collect(Collectors.toList());
    }

    public Stream<TNRoutesFileContext> getRoutesFiles(IntellijFileContext fileContext) {
        //Standard angular 1 routes
        Stream<TNAngularRoutesFileContext> routeFilesClassic = TNRoutesIndex.getAllAffectedFiles(fileContext.getProject(), fileContext.getAngularRoot().orElse(fileContext.getProjectDir())).stream()
                .map(psiFile -> new TNAngularRoutesFileContext(fileContext.getProject(), psiFile));

        //UI Routes tiny decorations routes
        Stream<TNRoutesFileContext> routeFilesUIRoutes = TN_UIRoutesIndex.getAllAffectedFiles(fileContext.getProject(), fileContext.getAngularRoot().orElse(fileContext.getProjectDir()))
                .stream().map(psiFile -> new TNUIRoutesFileContext(fileContext.getProject(), psiFile));

        //TODO for now we deal with only one route file per system
        //but in the long run we need to take care of a selection dialog
        //which allows to restoreSelection which route file
        return Stream.concat(routeFilesClassic, routeFilesUIRoutes);
    }

    @NotNull
    public Route getRoute(CreateRoute mainForm) {
        return new Route(
                mainForm.getTxtRouteName().getText(),
                mainForm.getTxtHref().getText(),
                (String) mainForm.getCbComponent().getSelectedItem(), this.getClass());
    }

    protected ComponentFileContext[] findAllPageComponents(IntellijFileContext rootContext) {

        List<PsiFile> foundFiles = ControllerIndex.getAllAffectedFiles(rootContext.getProject(), rootContext.getAngularRoot().orElse(rootContext.getProjectDir()));

        return foundFiles.stream()
                .flatMap(psiFile -> net.werpu.tools.supportive.fs.common.ComponentFileContext.getControllerInstances(new IntellijFileContext(rootContext.getProject(), psiFile)).stream())
                .toArray(size -> new ComponentFileContext[size]);

    }

    public Optional<ComponentFileContext> getDefaultComponentData(IntellijFileContext fileContext) {
        List<ComponentFileContext> editorControllers = (fileContext.getVirtualFile().isDirectory()) ? Collections.emptyList() : getControllers(fileContext);
        if (editorControllers.isEmpty()) {
            return empty();
        } else {
            return ofNullable(editorControllers.stream().findFirst().get());
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ComponentData {
        private ComponentFileContext componentFileContext;
        private String moduleName;
    }
}
