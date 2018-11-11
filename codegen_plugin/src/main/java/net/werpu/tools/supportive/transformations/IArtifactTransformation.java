package net.werpu.tools.supportive.transformations;

import java.io.IOException;

public interface IArtifactTransformation {
    String getTnDecTransformation() throws IOException;

    String getNgTransformation() throws IOException;
}
