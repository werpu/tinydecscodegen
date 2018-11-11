package net.werpu.tools.supportive.transformations;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModuleTransformation implements IArtifactTransformation {
    
    AngularJSModuleTransformationModel transformationModel;


    @Override
    public String getTnDecTransformation() {
        return "";
    }

    @Override
    public String getNgTransformation() {
        return "";
    }

}
