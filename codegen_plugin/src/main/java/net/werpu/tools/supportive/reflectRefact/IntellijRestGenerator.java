/*
 *
 *
 * Copyright 2019 Werner Punz
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

 */

package net.werpu.tools.supportive.reflectRefact;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.project.Project;
import net.werpu.tools.factories.TnDecGroupFactory;
import rest.RestService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.werpu.tools.factories.TnDecGroupFactory.TPL_TN_NG_REST_SERVICE;

public class IntellijRestGenerator {
    /**
     * Main entry point for the generator
     *
     * @param restServices the parsing model which needs to be templated
     *
     * @return a string with the service or errors in case of parsing errors
     */
    public static String generate(Project project, List<RestService> restServices, boolean ng) {
        final FileTemplate vslTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(ng ? TPL_TN_NG_REST_SERVICE : TnDecGroupFactory.TPL_TN_REST_SERVICE);

        return restServices.stream().map((RestService item) -> {
            try {

                Map<String, Object> root = new HashMap<>();
                root.put("service", item);

                return FileTemplateUtil.mergeTemplate(root, vslTemplate.getText(), false);

            } catch (IOException e) {
                return e.getMessage();
            }
        }).reduce("", (s0, s1) -> s0 + "\n" + s1);
    }
}
