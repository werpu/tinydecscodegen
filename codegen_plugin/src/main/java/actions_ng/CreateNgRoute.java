package actions_ng;

import actions_all.shared.ComponentSelectorModel;
import actions_all.shared.Messages;
import actions_all.shared.VisibleAssertions;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.psi.PsiFile;
import gui.CreateRoute;
import indexes.AngularIndex;
import indexes.ControllerIndex;
import indexes.NG_UIRoutesIndex;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.AngularVersion;
import supportive.fs.common.ComponentFileContext;
import supportive.fs.common.IntellijFileContext;
import supportive.fs.common.Route;
import supportive.fs.ng.NG_UIRoutesRoutesFileContext;
import supportive.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static actions_all.shared.FormAssertions.*;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class CreateNgRoute extends AnAction {


    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.ngVisible(anActionEvent);
        if(anActionEvent.getPresentation().isVisible() && !AngularIndex.isBelowAngularVersion(new IntellijFileContext(anActionEvent), AngularVersion.NG)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent event) {

        IntellijFileContext fileContext = new IntellijFileContext(event);
        final gui.CreateRoute mainForm = new gui.CreateRoute();
        final ComponentSelectorModel selectorModel;

        ComponentFileContext[] components = findAllPageComponents(fileContext);
        if (components.length == 0) {

            String message = "There was no component found, cannot create route automatically, please create it manually";
            supportive.utils.IntellijUtils.showErrorDialog(fileContext.getProject(), "Error",message);
            return;
        }

        List<ComponentFileContext> editorControllers = getControllers(fileContext);

        selectorModel = new ComponentSelectorModel(components);
        List<String> selectorIndex = Arrays.asList(selectorModel.getContextNames());
        mainForm.getCbComponent().setModel(new ListComboBoxModel(selectorIndex));
        Optional<ComponentFileContext> defaultComponentData = getDefaultComponentData(fileContext);
        if (editorControllers.size() > 0 || defaultComponentData.isPresent()) {
            ComponentFileContext cData = defaultComponentData.get();
            mainForm.getCbComponent().setSelectedIndex(selectorIndex.indexOf(cData.getDisplayName()));
            String componentClassName = cData.getClazzName();
            String displayName = cData.getDisplayName();
            mainForm.getCbComponent().setSelectedItem(displayName);
            mainForm.getTxtRouteName().setText(StringUtils.toLowerDash(componentClassName).replaceAll("_component", ""));
        } else {
            mainForm.updateRouteName(null);
        }

        DialogWrapper dialogWrapper = new DialogWrapper(fileContext.getProject(), true, DialogWrapper.IdeModalityType.PROJECT) {

            @Nullable
            @Override
            protected JComponent createCenterPanel() {
                return mainForm.getRootPanel();
            }

            @Nullable
            @Override
            protected String getDimensionServiceKey() {
                return "AnnRoute";
            }

            @Nullable
            @NotNull
            protected List<ValidationInfo> doValidateAll() {
                Route route = getRoute(mainForm, selectorModel);

                NG_UIRoutesRoutesFileContext ctx = NG_UIRoutesIndex.getAllAffectedFiles(fileContext.getProject(), fileContext.getAngularRoot().orElse(fileContext.getProjectDir())).stream()
                        .map(psiFile -> new NG_UIRoutesRoutesFileContext(fileContext.getProject(), psiFile)).findAny().get();

                return CreateNgRoute.this.validate(route, ctx, mainForm);
            }


            @Override
            public void init() {
                super.init();
            }

            public void show() {

                this.init();
                this.setModal(true);
                this.pack();
                super.show();
            }
        };



        dialogWrapper.setTitle("Create Route");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));

        dialogWrapper.show();


        if (dialogWrapper.isOK()) {
            Route route = getRoute(mainForm, selectorModel);
            ComponentFileContext compContext = components[mainForm.getCbComponent().getSelectedIndex()];

            getRoutesFiles(fileContext)
                    .forEach(rContext -> {
                        //calculate the component include relative from the file

                        WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {
                            try {
                                route.setComponentPath(compContext.calculateRelPathTo(rContext));
                                rContext.addRoute(route);

                                rContext.commit();
                                supportive.utils.IntellijUtils.showInfoMessage("The new route has been added", "Info");
                            } catch (IOException e) {
                                supportive.utils.IntellijUtils.showErrorDialog(fileContext.getProject(), "Error", e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    });
        }

    }

    @NotNull
    public Optional<ComponentFileContext> getDefaultComponentData(IntellijFileContext fileContext) {
        List<ComponentFileContext> editorControllers = (fileContext.getVirtualFile().isDirectory()) ? Collections.emptyList() : getControllers(fileContext);
        if(editorControllers.isEmpty()) {
            return empty();
        } else {
            return ofNullable(editorControllers.stream().findFirst().get());
        }
    }

    protected List<ValidationInfo> validate(Route route, NG_UIRoutesRoutesFileContext ctx, CreateRoute mainForm) {
        return Arrays.asList(
                assertNotNullOrEmpty(mainForm.getTxtRouteName().getText(), Messages.ERR_NAME_VALUE, mainForm.getTxtRouteName()),
                assertPattern(mainForm.getTxtRouteName().getText(), VALID_ROUTE, Messages.ERR_CONFIG_PATTERN, mainForm.getTxtRouteName()),
                assertTrue(!ctx.isRouteNameUsed(route), "The route name already is in use", mainForm.getTxtRouteName()),
                assertTrue(!ctx.isUrlInUse(route), "The route url already is in use", mainForm.getTxtHref())
        ).stream().filter(s -> s != null).collect(Collectors.toList());
    }

    public Stream<NG_UIRoutesRoutesFileContext> getRoutesFiles(IntellijFileContext fileContext) {
        return NG_UIRoutesIndex.getAllAffectedFiles(fileContext.getProject(), fileContext.getAngularRoot().orElse(fileContext.getProjectDir())).stream()
                .map(psiFile -> new NG_UIRoutesRoutesFileContext(fileContext.getProject(), psiFile));
    }

    @NotNull
    public Route getRoute(CreateRoute mainForm, ComponentSelectorModel selectorModel) {


        return new Route(
                        mainForm.getTxtRouteName().getText(),
                        mainForm.getTxtHref().getText(),
                        selectorModel.getComponentFileContexts()[mainForm.getCbComponent().getSelectedIndex()].getClazzName(), this.getClass());
    }


    protected ComponentFileContext[] findAllPageComponents(IntellijFileContext rootContext) {
        List<PsiFile> foundFiles = ControllerIndex.getAllAffectedFiles(rootContext.getProject(), rootContext.getAngularRoot().orElse(rootContext.getProjectDir()));

        return foundFiles.stream().flatMap(psiFile -> ComponentFileContext.getInstances(new IntellijFileContext(rootContext.getProject(), psiFile)).stream())
                .toArray(size -> new ComponentFileContext[size]);

    }


    //TODO element nearest
    public static List<ComponentFileContext> getControllers(IntellijFileContext ctx) {
        return ComponentFileContext.getInstances(ctx);
    }

}
