package actions_ng;

import actions.Messages;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.psi.PsiFile;
import gui.CreateRoute;
import indexes.ControllerIndex;
import indexes.RoutesIndex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.ComponentFileContext;
import supportive.fs.IntellijFileContext;
import supportive.fs.Route;
import supportive.fs.UIRoutesRoutesFileContext;
import supportive.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static actions.shared.FormAssertions.*;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
class ComponentSelectorModel {

    private int selectedIndex = 0;

    @NotNull
    private final ComponentFileContext[] componentFileContexts;


    public String[] getContextNames() {
        return Arrays.stream(componentFileContexts)
                .map(context -> context.getComponentClassName())
                .toArray(size -> new String[size]);
    }
}

public class CreateNgRoute extends AnAction {


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

                UIRoutesRoutesFileContext ctx = RoutesIndex.getAllMainRoutes(fileContext.getProject()).stream()
                        .map(psiFile -> new UIRoutesRoutesFileContext(fileContext.getProject(), psiFile)).findAny().get();



                return Arrays.asList(
                        assertNotNullOrEmpty(mainForm.getTxtRouteName().getText(), Messages.ERR_NAME_VALUE, mainForm.getTxtRouteName()),
                        assertPattern(mainForm.getTxtRouteName().getText(), VALID_ROUTE, Messages.ERR_CONFIG_PATTERN, mainForm.getTxtRouteName()),
                        assertTrue(!ctx.isRouteNameUsed(route), "The route name already is in use", mainForm.getTxtRouteName()),
                        assertTrue(!ctx.isUrlInUse(route), "The route url already is in use", mainForm.getTxtHref())
                ).stream().filter(s -> s != null).collect(Collectors.toList());
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
            PopupUtil.showBalloonForActiveFrame(message, MessageType.ERROR);
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
            //TODO perform refactoring operation here


            Route route = getRoute(mainForm);
            ComponentFileContext compContext = components[mainForm.getCbComponent().getSelectedIndex()];


            getRoutesFiles(fileContext)
                    .forEach(rContext -> {
                        //calculate the component include relative from the file
                        Path routesFilePath = Paths.get(rContext.getVirtualFile().getParent().getPath());
                        Path componentFilePath = Paths.get(compContext.getVirtualFile().getPath());
                        Path relPath = routesFilePath.relativize(componentFilePath);
                        route.setComponentPath("./" + relPath.toString());

                        rContext.addRoute(route);
                        WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {
                            try {
                                rContext.commit();
                            } catch (IOException e) {
                                //TODO error handling
                                e.printStackTrace();
                            }
                        });
                    });
        }

    }

    public Stream<UIRoutesRoutesFileContext> getRoutesFiles(IntellijFileContext fileContext) {
        return RoutesIndex.getAllMainRoutes(fileContext.getProject()).stream()
                .map(psiFile -> new UIRoutesRoutesFileContext(fileContext.getProject(), psiFile));
    }

    @NotNull
    public Route getRoute(CreateRoute mainForm) {
        return new Route(
                        mainForm.getTxtRouteName().getText(),
                        mainForm.getTxtHref().getText(),
                        (String) mainForm.getCbComponent().getSelectedItem());
    }


    private ComponentFileContext[] findAllPageComponents(IntellijFileContext rootContext) {
        List<PsiFile> foundFiles = ControllerIndex.getAllControllerFiles(rootContext.getProject());

        return foundFiles.stream().flatMap(psiFile -> ComponentFileContext.getInstances(new IntellijFileContext(rootContext.getProject(), psiFile)).stream())
                .toArray(size -> new ComponentFileContext[size]);

    }


    //TODO element nearest
    public static List<ComponentFileContext> getControllers(IntellijFileContext ctx) {
        return ComponentFileContext.getInstances(ctx);
    }

}
