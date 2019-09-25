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

package net.werpu.tools.components;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.util.io.FileUtil;
import net.werpu.tools.factories.TnDecGroupFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Application wide registration
 * <p>
 * for now we just register our file templates
 * defined by teh group which itself
 * is an extension point in the xml (wtf??)
 */
public class Application implements ApplicationComponent {
    public static final String TEMPLATE_PATH = "/fileTemplates/Tiny Decorations/";

    public Application() {
    }

    //we need to register the template file content
    private static void addFileTemplate(final @NonNls String name, @NonNls
            String ext) {

        FileTemplate template = FileTemplateManager.getDefaultInstance().getTemplate(name);
        if (template == null) {
            try {
                template =
                        FileTemplateManager.getDefaultInstance().addTemplate(name, ext);
                template.setText(FileUtil.loadTextAndClose(
                        new
                                InputStreamReader(Application.class.getResourceAsStream(TEMPLATE_PATH
                                + name + "." + TnDecGroupFactory.TPL_EXT + ".ft")))
                );
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }

    @Override
    public void initComponent() {
        addFileTemplate(TnDecGroupFactory.TPL_ANNOTATED_COMPONENT, TnDecGroupFactory.TPL_EXT);
        addFileTemplate(TnDecGroupFactory.TPL_ANNOTATED_CONTROLLER, TnDecGroupFactory.TPL_EXT);
    }

    @Override
    public void disposeComponent() {

    }

    @Override
    @NotNull
    public String getComponentName() {
        return "Tiny Decorations";
    }
}
