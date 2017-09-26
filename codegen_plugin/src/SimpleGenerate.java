import com.google.common.collect.Lists;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.fileEditor.DocumentsEditor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.apache.velocity.runtime.parser.ParseException;
import org.codehaus.plexus.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import reflector.SpringRestReflector;
import reflector.TypescriptRestGenerator;
import rest.RestService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

public class SimpleGenerate extends AnAction {

    private static final Logger log = Logger.getInstance(SimpleGenerate.class);

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if(editor == null) {
            log.error("No editor found, please focus on a source file with a rest endpoint");
            return;
        }

        Module module = getModuleFromEditor(project, editor);
        String className = getClassNameFromEditor(project, editor);


        //CompileStatusNotification compilerCallback = new CompileStatusNotification();
        CompilerManager.getInstance(project).compile(module, new CompileStatusNotification() {
            @Override
            public void finished(boolean b, int i, int i1, CompileContext compileContext) {
                ApplicationManager.getApplication().invokeLater(() -> compileDone(compileContext));
            }

            private boolean compileDone(CompileContext compileContext) {
                try {
                    VirtualFile virtualFile = compileContext.getModuleOutputDirectoryForTests(module);

                    List<URL> urls = Lists.newLinkedList();
                    if (addClassPath(compileContext, virtualFile, urls, module)) return false;

                    VirtualFile[] classessRoots = OrderEnumerator.orderEntries(module).recursively().getClassesRoots();
                    for(VirtualFile virtualFile1: classessRoots) {
                        urls.add(addClassPath(virtualFile1));
                    }



                    URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), this.getClass().getClassLoader());
                    Class compiledClass = urlClassLoader.loadClass(className);

                    List<RestService> restService = SpringRestReflector.reflect(Arrays.asList(compiledClass), true);
                    if(restService == null || restService.isEmpty()) {
                        log.error("No rest code found in selected file");
                        return false;
                    }
                    String text = TypescriptRestGenerator.generate("", restService);

                    String ext = ".ts";
                    String fileName = restService.get(0).getServiceName() + ext;

                    //PsiFile oldFile = PsiManager.getInstance(project).findFile(VirtualFileManager.getInstance().findFileByUrl());
                    PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(fileName, Language.findLanguageByID("TypeScript"),text);
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        moveFileToGeneratedDir(file, project, module);
                        FileEditorManager.getInstance(project).openFile(file.getVirtualFile(), true);
                    });


                    return true;
                } catch (RuntimeException | ParseException | IOException | ClassNotFoundException e) {
                    log.error(e);
                    Messages.showErrorDialog(project, e.getMessage(), "An Error has occurred");
                }
                return false;
            }
        });

       // String txt= Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon());
       // Messages.showMessageDialog(project, "Hello, " + txt + "!\n I am glad to see you.", "Information", Messages.getInformationIcon());
    }

    @NotNull
    private URL addClassPath(VirtualFile virtualFile1) throws MalformedURLException {
        String url = virtualFile1.getUrl();
        if(url.contains(".jar") && url.startsWith("jar://")) {
            url = url.replaceAll("jar:/","jar:file:/");
        }
        if(!url.endsWith("/")) {
            url = url + "/";
        }
        return new URL(url);
    }

    private boolean addClassPath(CompileContext compileContext, VirtualFile virtualFile, List<URL> urls, Module module) {
        try {
            urls.add(new URL(virtualFile.getUrl()+"/"));
            //Special case, older configuration often map their output dir to module/classes
            urls.add(new URL(virtualFile.getParent().getParent()+"/classes/"));
            urls.add(new URL(compileContext.getModuleOutputDirectoryForTests(module).getUrl()+"/"));
        } catch (MalformedURLException e) {
           log.error(e);
            return true;
        }
        return false;
    }

    private void moveFileToGeneratedDir(PsiFile file, Project project, Module module) {
        PsiDirectory dir = PsiDirectoryFactory.getInstance(project).createDirectory(module.getModuleFile().getParent());
        PsiDirectory rscDir = dir.findSubdirectory("generated-sources");
        if (rscDir == null) {
            String subDirStr = "target";
            rscDir = getOrCreate(dir, subDirStr);
            rscDir = getOrCreate(rscDir,"generated-sources");
            rscDir = getOrCreate(rscDir,"ts-ng-tinydecorations");
            rscDir = getOrCreate(rscDir,"services");
        }

        PsiFile oldFile = rscDir.findFile(file.getName());
        if(oldFile != null) {
            oldFile.delete();
        }
        rscDir.add(file);
    }

    private PsiDirectory getOrCreate(PsiDirectory dir, String subDirStr) {
        PsiDirectory findResult = dir.findSubdirectory(subDirStr);
        return findResult != null ? findResult :
        dir.createSubdirectory(subDirStr);
    }

    @NotNull
    private String getClassNameFromEditor(Project project, Editor editor) {
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        PsiJavaFile javaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(vFile);

        String packageName = javaFile.getPackageName();
        return packageName +"."+javaFile.getName().replaceAll(".java","");
    }

    private Module getModuleFromEditor(Project project, Editor editor) {
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        return ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(vFile);
    }
}
