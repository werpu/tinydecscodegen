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

import com.google.common.collect.Lists;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import utils.IntellijRefactor;
import utils.IntellijUtils;
import utils.ModuleElementScope;
import utils.RefactorUnit;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A reusable runnable which generates the final file
 * and adds the references in the parents module
 */
public class GenerateFileAndAddRef implements Runnable {

    Project project;
    VirtualFile folder;
    String className;
    FileTemplate vslTemplate;
    Map<String,Object> attrs;
    ModuleElementScope scope;

    public GenerateFileAndAddRef(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs, ModuleElementScope scope) {
        this.project = project;
        this.folder = folder;
        this.className = className;
        this.vslTemplate = vslTemplate;
        this.attrs = attrs;
        this.scope = scope;
    }

    @Override
    public void run() {
        try {

            String str = FileTemplateUtil.mergeTemplate(attrs, vslTemplate.getText(), false);
            String fileName = className + ".ts";

            String artifactType = IntellijRefactor.NG_MODULE;

            List<PsiFile> annotatedModules = IntellijUtils.findFirstAnnotatedClass(project, folder, artifactType);
            for (PsiFile module : annotatedModules) {
                String relativePath = folder.getPath().replaceAll(module.getVirtualFile().getParent().getPath(), ".");

                List<PsiElement> elements = IntellijRefactor.findAnnotatedElements(project, module, artifactType);
                List<RefactorUnit> refactoringsToProcess = elements.stream().map(element -> {
                    switch(scope) {
                        case EXPORT:
                            return refactorAddExport(className, module, element);
                        case DECLARATIONS:
                            return refactorAddDeclarations(className, module, element);
                        default:
                            return refactorAddImport(className, module, element);

                    }
                }).collect(Collectors.toList());


                List<RefactorUnit> finalRefactorings = Lists.newArrayList();
                addImport(className, module, relativePath, finalRefactorings);
                finalRefactorings.addAll(refactoringsToProcess);

                String refactoredText = IntellijRefactor.refactor(finalRefactorings);
                VirtualFile vModule = module.getVirtualFile();

                vModule.setBinaryContent(refactoredText.getBytes());
            }
            IntellijUtils.createAndOpen(project, folder, str, fileName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addImport(String className, PsiFile module, String relativePath, List<RefactorUnit> finalRefactorings) {
        finalRefactorings.add(IntellijRefactor.generateAppendAfterImport(module, "\nimport {" + className + "} from \"" + relativePath + "/" + className + "\";"));
    }

    @NotNull
    private RefactorUnit refactorAddExport(String className, PsiFile module, PsiElement element) {
        String elementText = element.getText();
        String rawData = elementText.substring(elementText.indexOf("(") + 1, elementText.lastIndexOf(")"));
        String refactoredData = IntellijRefactor.NG_MODULE + "(" + IntellijRefactor.appendExport(rawData, className) + ")";

        return new RefactorUnit(module, element, refactoredData);
    }

    @NotNull
    private RefactorUnit refactorAddImport(String className, PsiFile module, PsiElement element) {
        String elementText = element.getText();
        String rawData = elementText.substring(elementText.indexOf("(") + 1, elementText.lastIndexOf(")"));
        String refactoredData = IntellijRefactor.NG_MODULE + "(" + IntellijRefactor.appendImport(rawData, className) + ")";

        return new RefactorUnit(module, element, refactoredData);
    }

    @NotNull
    private RefactorUnit refactorAddDeclarations(String className, PsiFile module, PsiElement element) {
        String elementText = element.getText();
        String rawData = elementText.substring(elementText.indexOf("(") + 1, elementText.lastIndexOf(")"));
        String refactoredData = IntellijRefactor.NG_MODULE + "(" + IntellijRefactor.appendDeclare(rawData, className) + ")";

        return new RefactorUnit(module, element, refactoredData);
    }

}
