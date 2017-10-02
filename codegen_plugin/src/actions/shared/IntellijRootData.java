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
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import utils.IntellijUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * root data holder,
 * a class which determines the root data (aka module
 * and original java file from our ide context - aka open editor)
 * <p>
 * It either checks for a ref in a tyescript file
 * or determines the data from the given java file
 * <p>
 * If the data needed is found it is handed over
 * to the processing engine.
 */
public class IntellijRootData {

    private static final Logger log = Logger.getInstance(IntellijRootData.class);
    public static final String REF_DATA = "@ref:\\s([^\n\\s]+).*\n";
    public static final String ERR_NO_EDITOR = "No editor found, please focus on a source file with a java type";
    public static final String ERR_INVALID_REF = "The reference into the java file is invalid or outdated, no corresponding java file could be found.";
    public static final String ERR_NO_REF = "Selected Typescript file has no reference into java file";

    private boolean myResult;
    private AnActionEvent event;
    private Project project;
    private Module module;
    private String className;
    private PsiFile javaFile;

    public IntellijRootData(AnActionEvent event, Project project) {
        this.event = event;
        this.project = project;

        Editor editor = IntellijUtils.getEditor(event);
        if (editor == null) {
            errNoEditorFound();
            return;
        }

        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());

        if (isTsFile(vFile)) {
            fromTsFileToJavaFile(vFile);
            return;
        } else {
            fromEditorToJavaFile(editor);
            return;

        }
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

    public PsiFile getJavaFile() {
        return javaFile;
    }

    private boolean isTsFile(@NotNull VirtualFile vFile) {
        return PsiManager.getInstance(project).findFile(vFile).getFileType().getDefaultExtension().equalsIgnoreCase("ts");
    }

    @NotNull
    private IntellijRootData fromTsFileToJavaFile(VirtualFile vFile) {
        String content = PsiManager.getInstance(project).findFile(vFile).getText();
        Pattern pattern = Pattern.compile(REF_DATA);
        Matcher m = pattern.matcher(content);
        if (m.find()) {
            myResult = fromClassNameToJavaFile(m.group(1));
            return this;
        } else {
            return errNoRef();
        }
    }

    @NotNull
    private IntellijRootData fromEditorToJavaFile(Editor editor) {
        module = IntellijUtils.getModuleFromEditor(project, editor);
        className = IntellijUtils.getClassNameFromEditor(project, editor);
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        javaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(vFile);
        myResult = false;
        return this;
    }

    private boolean fromClassNameToJavaFile(String className) {
        this.className = className;
        PsiClass javaFile1 = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.projectScope(project));
        if (javaFile1 == null) {
            return errInvalidRef();
        }
        javaFile = javaFile1.getContainingFile();
        module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(javaFile.getVirtualFile());
        return false;
    }

    private boolean errInvalidRef() {
        Messages.showErrorDialog(project, "", ERR_INVALID_REF);
        myResult = true;
        return true;
    }

    @NotNull
    private IntellijRootData errNoEditorFound() {
        log.error(ERR_NO_EDITOR);
        myResult = true;
        return this;
    }

    @NotNull
    private IntellijRootData errNoRef() {
        Messages.showErrorDialog(project, "", ERR_NO_REF);
        myResult = true;
        return this;
    }
}