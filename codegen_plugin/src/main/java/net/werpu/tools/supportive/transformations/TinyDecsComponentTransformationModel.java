package net.werpu.tools.supportive.transformations;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;

public class TinyDecsComponentTransformationModel extends TypescriptFileContext implements ITransformationModel {
    public TinyDecsComponentTransformationModel(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public TinyDecsComponentTransformationModel(AnActionEvent event) {
        super(event);
    }

    public TinyDecsComponentTransformationModel(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public TinyDecsComponentTransformationModel(IntellijFileContext fileContext) {
        super(fileContext);
    }


}
