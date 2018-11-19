package net.werpu.tools.actions_all.shared;

import com.intellij.openapi.editor.Editor;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.IArtifactTransformation;

public interface TransformationInvoker {
    public Runnable createAction(IntellijFileContext fileContext, Editor editor, IArtifactTransformation model);
}
