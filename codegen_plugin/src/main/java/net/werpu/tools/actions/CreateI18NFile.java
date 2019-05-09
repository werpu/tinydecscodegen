package net.werpu.tools.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import net.werpu.tools.actions_all.shared.Messages;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.gui.support.InputDialogWrapperBuilder;
import net.werpu.tools.gui.support.ValidatableDialogWrapper;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.IntellijRunUtils;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.werpu.tools.actions_all.shared.FormAssertions.assertNotNullOrEmpty;

/**
 * Creates an I18N File for further processing
 */
public class CreateI18NFile extends AnAction {

    private static final Dimension PREFERRED_SIZE = new Dimension(400, 300);
    private static final String DEFAULT_TS_CONTENT = "/** \n* @def: i18nfile \n*/ \nexport const language = {\n};";
    private static final String DEFAULT_JSON_CONTENT = "{\n}";


    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        VisibleAssertions.templateVisible(anActionEvent);
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);

        if (IntellijUtils.getFolderOrFile(anActionEvent) == null ||
                !ctx.getVirtualFile().isDirectory()) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }
        VisibleAssertions.tnVisible(anActionEvent);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        IntellijFileContext ctx = new IntellijFileContext(e);
        final net.werpu.tools.gui.CreateI18NFile mainForm = new net.werpu.tools.gui.CreateI18NFile();

        String DIMENSION_KEY = "CreateI18NFile";
        ValidatableDialogWrapper dialogWrapper = (ValidatableDialogWrapper) new InputDialogWrapperBuilder(ctx.getProject(), mainForm.getRootPanel())
                .withDimensionKey(DIMENSION_KEY)
                .withTitle("Create I18N File")
                .withValidator(validationCallback(ctx, mainForm))
                .withPreferredSize(PREFERRED_SIZE)
                .create();

        mainForm.getTxtFilename().setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent jComponent) {
                List validationInfo = validationCallback(ctx, mainForm).get();
                return validationInfo.isEmpty();
            }
        });



        dialogWrapper.show();

        if (dialogWrapper.isOK()) {
            String[] fileEndings = getEndings(mainForm);
            String fileName = mainForm.getTxtFilename().getText();
            IntellijRunUtils.invokeLater(() -> IntellijRunUtils.writeTransaction(ctx.getProject(), () -> {
                    boolean success = Arrays.stream(fileEndings)
                            .map(ending -> {
                                try {
                                    createI18NFile(ctx, fileName, ending);
                                } catch (IOException ex) {
                                    IntellijUtils.handleEx(ctx.getProject(), ex);
                                    return false;
                                }
                                return true;

                            }).reduce( (result1, result2) -> result1 && result2).orElse(false);
                    if(success) {
                        IntellijUtils.showInfoMessage("I18N Files have been generated", "Success");
                    }
            }));
        }
    }

    /**
     * central i18n file creation method, which has all the rules necessary to create a file
     * @throws IOException in case of a file ops failure
     */
    private void createI18NFile(IntellijFileContext ctx, String fileName, String ending) throws IOException {
        if (ending.equalsIgnoreCase(".ts")) {
            IntellijUtils.create(ctx.getProject(), ctx.getVirtualFile(), DEFAULT_TS_CONTENT, fileName + ending);
        } else {
            IntellijUtils.create(ctx.getProject(), ctx.getVirtualFile(), DEFAULT_JSON_CONTENT, fileName + ending);
        }
    }


    @NotNull
    private Supplier<List<ValidationInfo>> validationCallback(IntellijFileContext ctx, net.werpu.tools.gui.CreateI18NFile mainForm) {
        return () -> {
            List<ValidationInfo> retList = new ArrayList<>();
            retList.add(assertNotNullOrEmpty(mainForm.getTxtFilename().getText(), Messages.ERR_EMPTY_FILENAME, mainForm.getTxtFilename()));

            String[] endingsToCheck = getEndings(mainForm);

            retList.add(assertExists(ctx, mainForm.getTxtFilename().getText(), mainForm.getTxtFilename(), endingsToCheck));

            return retList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        };

    }

    @NotNull
    private String[] getEndings(net.werpu.tools.gui.CreateI18NFile mainForm) {
        String[] endingsToCheck = new String[0];
        if (mainForm.getRbTypescript().isSelected()) {
            endingsToCheck = new String[]{".ts"};
        }
        if (mainForm.getRbJson().isSelected()) {
            endingsToCheck = new String[]{".json"};
        }
        if (mainForm.getRbBoth().isSelected()) {
            endingsToCheck = new String[]{".json", ".ts"};
        }
        return endingsToCheck;
    }

    @Nullable
    private ValidationInfo assertExists(IntellijFileContext rootDir, String fileName, JComponent affectedComponent, String... endings) {
        boolean exists = !rootDir.getChildren((VirtualFile child) -> {
            if (child.isDirectory()) {
                return false;
            }
            String childName = child.getName();
            return Arrays.stream(endings)
                    .map(ending -> (childName.equalsIgnoreCase(fileName + ending)))
                    .reduce((first, second) -> first || second)
                    .orElse(false);
        }).isEmpty();
        if (exists) {
            return new ValidationInfo(Messages.ERR_FILE_EXISTS, affectedComponent);
        }
        return null;

    }
}
