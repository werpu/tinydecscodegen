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
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import reflector.SpringJavaRestReflector;
import reflector.TypescriptRestGenerator;
import rest.RestService;
import utils.IntellijUtils;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;


/**
 * An intellij action to generate a typescript service
 * out of the currently open editors content, if
 * the service is a spring rest service at all
 */
public class ServiceGenerationAction extends AnAction {

    private static final Logger log = Logger.getInstance(ServiceGenerationAction.class);

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = IntellijUtils.getProject(event);
        Editor editor = IntellijUtils.getEditor(event);
        if(editor == null) {
            log.error("No editor found, please focus on a source file with a rest endpoint");
            return;
        }

        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        PsiJavaFile javaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(vFile);
        PsiClass clz = javaFile.getClasses()[0];

        //clz.getContainingFile()
        //clz.getAllMethods()[3].getParameterList().getParameters()[0].getModifierList().getAnnotations()[0].getParameterList().getAttributes()[0].getValue().

        Module module = IntellijUtils.getModuleFromEditor(project, editor);
        String className = IntellijUtils.getClassNameFromEditor(project, editor);


        //CompileStatusNotification compilerCallback = new CompileStatusNotification();
        CompilerManager.getInstance(project).compile(module, new CompileStatusNotification() {
            @Override
            public void finished(boolean b, int i, int i1, CompileContext compileContext) {
                ApplicationManager.getApplication().invokeLater(() -> compileDone(compileContext));
            }

            private boolean compileDone(CompileContext compileContext) {
                try {
                    URLClassLoader urlClassLoader = IntellijUtils.getClassLoader(compileContext, module);
                    Class compiledClass = urlClassLoader.loadClass(className);

                    List<RestService> restService = SpringJavaRestReflector.reflect(Arrays.asList(compiledClass), true);
                    if(restService == null || restService.isEmpty()) {
                        log.error("No rest code found in selected file");
                        return false;
                    }
                    String text = TypescriptRestGenerator.generate(restService);

                    String ext = ".ts";
                    String fileName = restService.get(0).getServiceName() + ext;

                    IntellijUtils.generateNewTypescriptFile(text, fileName, project, module);


                    return true;
                } catch (RuntimeException |  IOException | ClassNotFoundException e) {
                    log.error(e);
                    Messages.showErrorDialog(project, e.getMessage(), "An Error has occurred");
                }
                return false;
            }
        });

     }

}
