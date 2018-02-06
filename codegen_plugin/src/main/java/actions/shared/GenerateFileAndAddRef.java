/*

Copyright 2017 Werner Punz

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software
is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package actions.shared;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import supportive.dtos.ModuleElementScope;
import supportive.fs.IntellijFileContext;
import supportive.utils.IntellijUtils;

import java.io.IOException;
import java.util.Map;

import static supportive.reflectRefact.IntellijRefactor.*;

/**
 * A reusable runnable which generates the final file
 * and adds the references in the parents module
 */
public class GenerateFileAndAddRef implements Runnable {

    Project project;
    VirtualFile folder;
    String className;
    FileTemplate vslTemplate;
    Map<String, Object> attrs;
    ModuleElementScope[] scope;

    FileNameTransformer fileNameTransformer;

    protected GenerateFileAndAddRef(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs, FileNameTransformer fileNameTransformer) {
        this.project = project;
        if(!folder.isDirectory()) {
            folder = folder.getParent();
        }
        this.folder = folder;
        this.className = className;
        this.vslTemplate = vslTemplate;
        this.attrs = attrs;
        this.fileNameTransformer = fileNameTransformer;
    }



    public GenerateFileAndAddRef(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs, FileNameTransformer fileNameTransformer, ModuleElementScope ... scope) {
        this(project, folder, className, vslTemplate, attrs, fileNameTransformer);
        this.scope = scope;
    }

    @Override
    public void run() {
        try {

            String str = FileTemplateUtil.mergeTemplate(attrs, vslTemplate.getText(), false);
            String fileName = getFileName();

            IntellijFileContext fileContext = new IntellijFileContext(project, folder);

            for(ModuleElementScope singleScope: scope) {
                appendDeclarationToModule(fileContext, singleScope, className, getFileName());
            }

            IntellijUtils.createAndOpen(project, folder, str, fileName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private String getFileName() {
        return fileNameTransformer.transform(className);
    }

}
