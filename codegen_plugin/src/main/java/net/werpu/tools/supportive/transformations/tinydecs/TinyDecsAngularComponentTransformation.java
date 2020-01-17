/*
 *
 *
 * Copyright 2020 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * /
 */

package net.werpu.tools.supportive.transformations.tinydecs;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.graph.util.HashMap2D;
import net.werpu.tools.supportive.transformations.shared.IArtifactTransformation;
import net.werpu.tools.supportive.transformations.shared.ITransformationModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.werpu.tools.factories.TnDecGroupFactory.TPL_TN_DEC_ANGULAR_COMPONENT_TRANSFORMATION;
import static net.werpu.tools.factories.TnDecGroupFactory.TPL_TN_DEC_COMPONENT_TRANSFORMATION;

/**
 * Transformation controller which maps the tiny decs meta data with a
 * template into a tiny decs component
 * We reuse a velocity template as well for this one, given that the heavy lifting must be done in the transformation
 * model/controller itself
 */
public class TinyDecsAngularComponentTransformation implements IArtifactTransformation {

    TinyDecsComponentTransformationModel transformationModel;

    @Override
    public String getTnDecTransformation() throws IOException {
        return null;
    }

    @Override
    public String getNgTransformation() throws IOException {
        Map<String, Object> attrs = new HashMap();
        attrs.put("data", transformationModel);

        String text = FileTemplateUtil.mergeTemplate(attrs, getNgTemplate().getText(), false);
        return text;
    }

    @Override
    public ITransformationModel getTransformationModel() {
        return transformationModel;
    }

    protected FileTemplate getNgTemplate() {
        return FileTemplateManager.getInstance(transformationModel.getProject()).getJ2eeTemplate(TPL_TN_DEC_ANGULAR_COMPONENT_TRANSFORMATION);
    }



}
