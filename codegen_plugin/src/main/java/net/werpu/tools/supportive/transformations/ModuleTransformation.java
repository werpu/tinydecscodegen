package net.werpu.tools.supportive.transformations;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.factories.TnDecGroupFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ModuleTransformation implements IArtifactTransformation {
    
    AngularJSModuleTransformationModel transformationModel;


    @Override
    public String getTnDecTransformation() throws IOException {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("data", transformationModel);
        attrs.put("import", "import {Inject, NgModule} from \"TinyDecorations\";");

        String text = FileTemplateUtil.mergeTemplate(attrs, getTnTemplate().getText(), false);
        return text;
    }


    @Override
    public String getNgTransformation() {
        return "";
    }


    protected FileTemplate getTnTemplate() {
        return FileTemplateManager.getInstance(transformationModel.getProject()).getJ2eeTemplate(TnDecGroupFactory.TPL_TN_DEC_MODULE_TRANSFORMATION);
    }


}
