package actions_ng;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.psi.PsiFile;

import indexes.ComponentIndex;
import indexes.ControllerIndex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.ComponentFileContext;
import supportive.fs.IntellijFileContext;
import supportive.utils.IntellijUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
class ComponentSelectorModel {

    private int selectedIndex = 0;

    @NotNull
    private final ComponentFileContext [] componentFileContexts;



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
                /*return Arrays.asList(
                        assertNotNullOrEmpty(mainForm.getName(), Messages.ERR_NAME_VALUE, mainForm.getTxtName()),
                        assertPattern(mainForm.getName(), VALID_NAME, Messages.ERR_CONFIG_PATTERN, mainForm.getTxtName())
                ).stream().filter(s -> s != null).collect(Collectors.toList());*/
                return Collections.emptyList();
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
        if(components.length == 0) {

            String message = "There was no component found, cannot create route automatically, please create it manually";
            PopupUtil.showBalloonForActiveFrame(message, MessageType.ERROR);
            return;
        }
        selectorModel = new ComponentSelectorModel(components);

        mainForm.getCbComponent().setModel(new ListComboBoxModel(Arrays.asList(selectorModel.getContextNames())));

        dialogWrapper.setTitle("Create Route");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));
        dialogWrapper.show();



        if(dialogWrapper.isOK()) {
            //TODO perform refactoring operation here
        }

    }


    private ComponentFileContext[] findAllPageComponents(IntellijFileContext rootContext) {
        List<PsiFile> foundFiles = ControllerIndex.getAllControllerFiles(rootContext.getProject());

        return foundFiles.stream().flatMap(psiFile -> ComponentFileContext.getInstances(new IntellijFileContext(rootContext.getProject(), psiFile)).stream())
                .toArray(size -> new ComponentFileContext[size]);

    }

    //List<IntellijFileContext> getPossibleComponentCandidates(IntellijFileContext rootContext) {

        //now we have found the files we need to parse the class names, we have to search for the component annotation and then step
        //down from there to the first class definition
        //foundFiles.stream().map(el -> new PsiElementContext(el))
        //        .flatMap(elc -> elc.findPsiElement(el -> æ))

    //}
}
