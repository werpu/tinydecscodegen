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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import utils.IntellijUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntellijRootData {

    private static final Logger log = Logger.getInstance(IntellijRootData.class);

    private boolean myResult;
    private AnActionEvent event;
    private Project project;
    private Module module;
    private String className;

    public IntellijRootData(AnActionEvent event, Project project) {
        this.event = event;
        this.project = project;
    }

    public boolean isError() {
        return myResult;
    }

    public Module getModule() {
        return module;
    }

    public String getClassName() {
        return className;
    }

    public IntellijRootData invoke() {
        Editor editor = IntellijUtils.getEditor(event);
        if (editor == null) {
            log.error("No editor found, please focus on a source file with a java type");
            myResult = true;
            return this;
        }

        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        boolean isTsFile = PsiManager.getInstance(project).findFile(vFile).getFileType().getDefaultExtension().equalsIgnoreCase("ts");
        PsiJavaFile javaFile = null;


        module = null;
        className = null;
        if (isTsFile) {
            String content = PsiManager.getInstance(project).findFile(vFile).getText();
            Pattern pattern = Pattern.compile("@ref:\\s([^\n\\s]+).*\n");
            Matcher m = pattern.matcher(content);
            if (m.find()) {
                //package found
                //FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.containsFileOfType(JavaFileType.INSTANCE), JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
                className = m.group(1);
                PsiClass javaFile1 = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.projectScope(project));
                javaFile = (PsiJavaFile) javaFile1.getContainingFile();
                module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(javaFile.getVirtualFile());
            } else {
                Messages.showErrorDialog(project, "", "Selected Typescript file has no reference into java file");
                myResult = true;
                return this;
            }
        } else {

            module = IntellijUtils.getModuleFromEditor(project, editor);
            className = IntellijUtils.getClassNameFromEditor(project, editor);
        }
        myResult = false;
        return this;
    }
}