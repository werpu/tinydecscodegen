package net.werpu.tools.supportive.transformations;

import net.werpu.tools.gui.Refactoring;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;

public interface ITransformationCreator {
    public IArtifactTransformation apply(IntellijFileContext ctx, Refactoring mainForm);
}
