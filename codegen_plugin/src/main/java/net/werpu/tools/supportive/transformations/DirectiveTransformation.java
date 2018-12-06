package net.werpu.tools.supportive.transformations;


import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.werpu.tools.factories.TnDecGroupFactory.TPL_TN_DEC_COMPONENT_TRANSFORMATION;
import static net.werpu.tools.factories.TnDecGroupFactory.TPL_TN_DEC_DIRECTIVE_TRANSFORMATION;

/**
 * Component transformation
 * which binds the templates to the data
 * and does some necessary transformationModel transformations
 * so that the template can pick the needed data up
 */
@AllArgsConstructor
@Getter
public class DirectiveTransformation implements IArtifactTransformation {
    ITransformationModel transformationModel;

    @Override
    public String getTnDecTransformation() throws IOException {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("data", transformationModel);
        attrs.put("import", "import {Component, Controller} from \"TinyDecorations\";");

        String text = FileTemplateUtil.mergeTemplate(attrs, getTnTemplate().getText(), false);
        return text;
    }

    @Override
    public String getNgTransformation() throws IOException {
        return "TODO";
    }


    protected FileTemplate getTnTemplate() {
        return FileTemplateManager.getInstance(transformationModel.getProject()).getJ2eeTemplate(TPL_TN_DEC_DIRECTIVE_TRANSFORMATION);
    }
}
