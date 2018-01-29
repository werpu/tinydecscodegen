package actions_ng;

import actions.ServiceGenerationAction;
import actions.shared.JavaFileContext;
import actions.shared.NgFileNameTransformer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.psi.PsiJavaFile;
import utils.IntellijUtils;

public class ServiceGenerateActionFromSource extends AnAction {

    private static final Logger log = Logger.getInstance(ServiceGenerationAction.class);

    @Override
    public void actionPerformed(AnActionEvent event) {
        if(event.getData(PlatformDataKeys.EDITOR) == null) {
            PopupUtil.showBalloonForActiveFrame("No editor found, please focus on an open source file", MessageType.ERROR);
            return;
        }

        final JavaFileContext javaData = new JavaFileContext(event);
        if (javaData.isError()) return;

        try {
            IntellijUtils.fileNameTransformer = new NgFileNameTransformer("service");
            IntellijUtils.generateService(javaData.getProject(), javaData.getModule(),(PsiJavaFile) javaData.getJavaFile(), true);
        } catch (RuntimeException |  ClassNotFoundException e) {
            log.error(e);
            Messages.showErrorDialog(javaData.getProject(), e.getMessage(), actions.Messages.ERR_OCCURRED);
        }

    }
}

