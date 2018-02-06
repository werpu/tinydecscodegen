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
package utils.fs;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import reflector.utils.ReflectUtils;
import utils.DummyInsertPsiElement;
import utils.IRefactorUnit;
import utils.IntellijUtils;
import utils.RefactorUnit;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * special file context for typescript files
 * which has typescript refactoring capabilities
 */
public class TypescriptFileContext extends IntellijFileContext {

    private List<IRefactorUnit> refactorUnits = Lists.newArrayList();

    public TypescriptFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public TypescriptFileContext(AnActionEvent event) {
        this(event.getProject(), IntellijUtils.getFolderOrFile(event));
    }


    public TypescriptFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }


    public TypescriptFileContext(IntellijFileContext fileContext) {
        super(fileContext.getProject(), fileContext.getPsiFile());
    }


    public TypescriptFileContext generateImport(String className, PsiFile module, String relativePath) {
        String reducedClassName = ReflectUtils.reduceClassName(className);
        String importStatement = "\nimport {" + reducedClassName + "} from \"" + relativePath + "/" + reducedClassName + "\";";
        if(module.getText().contains(importStatement)) { //skip if existing
            refactorUnits.add(new RefactorUnit(module, new DummyInsertPsiElement(0), ""));
            return this;
        }
        return appendImport(importStatement);
    }


    public TypescriptFileContext appendImport(String newImport) {
        Pattern p = Pattern.compile("((import[^\\n\\;]+)\\s+[=from]+\\s+[^\\n\\;]+[\\n\\;])");
        Matcher m = p.matcher(psiFile.getText());
        int end = 0;

        while (m.find())
        {
            end = m.end(1);
        }

        refactorUnits.add(new RefactorUnit(psiFile, new DummyInsertPsiElement(end), newImport));
        return this;
    }


    public void addRefactoring(RefactorUnit unit) {
        this.refactorUnits.add(unit);
    }


    @Override
    public void commit() throws IOException {

        refactorUnits = refactorUnits.stream()
                .sorted(Comparator.comparing(a -> Integer.valueOf(a.getStartOffset())))
                .collect(Collectors.toList());

        super.refactorContent(refactorUnits);
        super.commit();
    }

    public void rollback() {
        this.refactorUnits = Lists.newLinkedList();
    }

    //TODO a bunch of helper methods to detect artifact positions more
    //quickly
}
