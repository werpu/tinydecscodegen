/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.werpu.tools.supportive.transformations.tinydecs;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.supportive.transformations.shared.IArtifactTransformation;
import net.werpu.tools.supportive.transformations.shared.ITransformationModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.werpu.tools.factories.TnDecGroupFactory.TPL_TN_DEC_ANGULAR_COMPONENT_TRANSFORMATION;
import static net.werpu.tools.factories.TnDecGroupFactory.TPL_TN_DEC_COMPONENT_TRANSFORMATION;

@Getter
@AllArgsConstructor
public class TnAngularComponentTransformation implements IArtifactTransformation {
    ITransformationModel transformationModel;

    @Override
    public String getTnDecTransformation() throws IOException {
        return "TODO";
    }

    @Override
    public String getNgTransformation() throws IOException {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("data", transformationModel);
        attrs.put("import", "import {Component, Controller, Input, Output} from \"angular\";");

        String text = FileTemplateUtil.mergeTemplate(attrs, getTnTemplate().getText(), false);
        return text;
    }

    protected FileTemplate getTnTemplate() {
        return FileTemplateManager.getInstance(transformationModel.getProject()).getJ2eeTemplate(TPL_TN_DEC_ANGULAR_COMPONENT_TRANSFORMATION);
    }
}
