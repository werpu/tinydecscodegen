package actions;

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
import indexes.ControllerIndex;
import indexes.RoutesIndex;
import indexes.TNRoutesIndex;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.ComponentFileContext;
import supportive.fs.common.IntellijFileContext;
import supportive.fs.common.Route;
import supportive.fs.ng.UIRoutesRoutesFileContext;
import supportive.fs.tn.TNUIRoutesRoutesFileContext;
import supportive.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static actions_all.shared.FormAssertions.*;

public class CreateTnDecRoute extends AnAction {

    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.ngVisible(anActionEvent);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        IntellijFileContext fileContext = new IntellijFileContext(event);
        final gui.CreateRoute mainForm = new gui.CreateRoute();
        ComponentSelectorModel selectorModel = null;


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
                Route route = getRoute(mainForm);

                UIRoutesRoutesFileContext ctx = RoutesIndex.getAllMainRoutes(fileContext.getProject(), fileContext.getAngularRoot().orElse(fileContext.getProjectDir())).stream()
                        .map(psiFile -> new UIRoutesRoutesFileContext(fileContext.getProject(), psiFile)).findAny().get();


                return CreateTnDecRoute.this.validate(route, ctx, mainForm);
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

        ComponentFileContext[] components = findAllPageComponents(fileContext);
        if (components.length == 0) {

            String message = "There was no component found, cannot create route automatically, please create it manually";
            supportive.utils.IntellijUtils.showErrorDialog(fileContext.getProject(), "Error", message);
            return;
        }

        List<ComponentFileContext> editorControllers = getControllers(fileContext);

        selectorModel = new ComponentSelectorModel(components);
        mainForm.getCbComponent().setModel(new ListComboBoxModel(Arrays.asList(selectorModel.getContextNames())));
        if (editorControllers.size() > 0) {
            String componentClassName = editorControllers.stream().findFirst().get().getComponentClassName();
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

            getRoutesFiles(fileContext)
                    .forEach(rContext -> {
                        //calculate the component include relative from the file
                        route.setComponentPath(compContext.calculateRelPathTo(rContext));
                        rContext.addRoute(route);

                        WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {
                            try {
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


    //dedup this code
    protected List<ValidationInfo> validate(Route route, UIRoutesRoutesFileContext ctx, CreateRoute mainForm) {
        return Arrays.asList(
                assertNotNullOrEmpty(mainForm.getTxtRouteName().getText(), Messages.ERR_NAME_VALUE, mainForm.getTxtRouteName()),
                assertPattern(mainForm.getTxtRouteName().getText(), VALID_ROUTE, Messages.ERR_CONFIG_PATTERN, mainForm.getTxtRouteName()),
                assertTrue(!ctx.isRouteNameUsed(route), "The route name already is in use", mainForm.getTxtRouteName()),
                assertTrue(!ctx.isUrlInUse(route), "The route url already is in use", mainForm.getTxtHref())
        ).stream().filter(s -> s != null).collect(Collectors.toList());
    }

    public Stream<TNUIRoutesRoutesFileContext> getRoutesFiles(IntellijFileContext fileContext) {
        return TNRoutesIndex.getAllMainRoutes(fileContext.getProject(), fileContext.getAngularRoot().orElse(fileContext.getProjectDir())).stream()
                .map(psiFile -> new TNUIRoutesRoutesFileContext(fileContext.getProject(), psiFile));
    }

    @NotNull
    public Route getRoute(CreateRoute mainForm) {
        return new Route(
                mainForm.getTxtRouteName().getText(),
                mainForm.getTxtHref().getText(),
                (String) mainForm.getCbComponent().getSelectedItem());
    }

    protected ComponentFileContext[] findAllPageComponents(IntellijFileContext rootContext) {

        List<PsiFile> foundFiles = ControllerIndex.getAllControllerFiles(rootContext.getProject(), rootContext.getAngularRoot().orElse(rootContext.getProjectDir()));

        return foundFiles.stream()
                .flatMap(psiFile -> supportive.fs.common.ComponentFileContext.getControllerInstances(new IntellijFileContext(rootContext.getProject(), psiFile)).stream())
                .toArray(size -> new ComponentFileContext[size]);

    }

    //TODO element nearest
    public static List<ComponentFileContext> getControllers(IntellijFileContext ctx) {
        return ComponentFileContext.getInstances(ctx);
    }

}
