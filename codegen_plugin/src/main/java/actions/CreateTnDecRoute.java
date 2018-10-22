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
import gui.support.DialogWrapperCreator;
import indexes.ControllerIndex;
import indexes.TNRoutesIndex;
import indexes.TN_UIRoutesIndex;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import supportive.fs.common.ComponentFileContext;
import supportive.fs.common.IntellijFileContext;
import supportive.fs.common.Route;
import supportive.fs.tn.TNAngularRoutesFileContext;
import supportive.fs.tn.TNRoutesFileContext;
import supportive.fs.tn.TNUIRoutesFileContext;
import supportive.utils.StringUtils;

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

public class CreateTnDecRoute extends AnAction {

    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.tnVisible(anActionEvent);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        IntellijFileContext fileContext = new IntellijFileContext(event);
        final gui.CreateRoute mainForm = new gui.CreateRoute();
        ComponentSelectorModel selectorModel = null;


        DialogWrapper dialogWrapper = new DialogWrapperCreator(fileContext.getProject(), mainForm.getRootPanel())
                .withDimensionKey("AnnRoute").withValidator(() -> {
                    Route route = getRoute(mainForm);
                    return getRoutesFiles(fileContext).flatMap(el -> CreateTnDecRoute.this.validate(route, el, mainForm).stream()).collect(Collectors.toList());
                }).create();


        ComponentFileContext[] components = findAllPageComponents(fileContext);
        if (components.length == 0) {

            String message = "There was no component found, cannot create route automatically, please create it manually";
            supportive.utils.IntellijUtils.showErrorDialog(fileContext.getProject(), "Error", message);
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
            String componentClassName = cData.getComponentClassName();
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
                                    Route myRoute = (Route) route.clone();
                                    myRoute.setComponentPath(compContext.calculateRelPathTo(rContext));
                                    myRoute.setComponent(myRoute.getComponent().replaceAll("\\[[^\\]]+\\]", ""));
                                    rContext.addRoute(myRoute);


                                    rContext.commit();
                                    rContext.reformat();

                                } catch (IOException | CloneNotSupportedException e) {
                                    throw new RuntimeException(e);
                                }

                            });
                    supportive.utils.IntellijUtils.showInfoMessage("The new route has been added", "Info");
                } catch (RuntimeException e) {
                    supportive.utils.IntellijUtils.showErrorDialog(fileContext.getProject(), "Error", e.getMessage());
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
        Stream<TNAngularRoutesFileContext> routeFilesClassic = TNRoutesIndex.getAllMainRoutes(fileContext.getProject(), fileContext.getAngularRoot().orElse(fileContext.getProjectDir())).stream()
                .map(psiFile -> new TNAngularRoutesFileContext(fileContext.getProject(), psiFile));

        //UI Routes tiny decorations routes
        Stream<TNRoutesFileContext> routeFilesUIRoutes = TN_UIRoutesIndex.getAllMainRoutes(fileContext.getProject(), fileContext.getAngularRoot().orElse(fileContext.getProjectDir()))
                .stream().map(psiFile -> new TNUIRoutesFileContext(fileContext.getProject(), psiFile));

        //TODO for now we deal with only one route file per system
        //but in the long run we need to take care of a selection dialog
        //which allows to select which route file
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

        List<PsiFile> foundFiles = ControllerIndex.getAllControllerFiles(rootContext.getProject(), rootContext.getAngularRoot().orElse(rootContext.getProjectDir()));

        return foundFiles.stream()
                .flatMap(psiFile -> supportive.fs.common.ComponentFileContext.getControllerInstances(new IntellijFileContext(rootContext.getProject(), psiFile)).stream())
                .toArray(size -> new ComponentFileContext[size]);

    }

    //TODO element nearest
    public static List<ComponentFileContext> getControllers(IntellijFileContext ctx) {
        return ComponentFileContext.getInstances(ctx);
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
