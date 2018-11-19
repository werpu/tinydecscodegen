package net.werpu.tools.supportive.transformations;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

/**
 * Placeholder for the component transformation
 * which works on the
 */
@AllArgsConstructor
@Getter
public class ComponentTransformation implements IArtifactTransformation {
    ITransformationModel model;

    @Override
    public String getTnDecTransformation() throws IOException {
        return null;
    }

    @Override
    public String getNgTransformation() throws IOException {
        return null;
    }
}
