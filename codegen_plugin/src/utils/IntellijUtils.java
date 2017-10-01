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
package utils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import gui.Confirm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reflector.SpringJavaRestReflector;
import reflector.TypescriptDtoGenerator;
import reflector.TypescriptRestGenerator;
import reflector.utils.ReflectUtils;
import rest.GenericClass;
import rest.RestService;

import javax.naming.spi.DirectoryManager;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * utils class to simplify some of the intelli openapi apis
 */
public class IntellijUtils {

    private static final Logger log = Logger.getInstance(IntellijUtils.class);


    public static void generateNewTypescriptFile(String text, String fileName, Project project, Module module) {
        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(fileName, Language.findLanguageByID("TypeScript"), text);
        ApplicationManager.getApplication().runWriteAction(() -> {
            moveFileToGeneratedDir(file, project, module);
            FileEditorManager.getInstance(project).openFile(file.getVirtualFile(), true);
        });
    }

    public static URLClassLoader getClassLoader(CompileContext compileContext, Module module) throws MalformedURLException {
        VirtualFile virtualFile = compileContext.getModuleOutputDirectoryForTests(module);

        List<URL> urls = Lists.newLinkedList();
        addClassPath(compileContext, virtualFile, urls, module);

        return getModuleClassLoader(module, urls);
    }

    public static URLClassLoader getClassLoader(Module module) throws MalformedURLException {
        List<URL> urls = Lists.newLinkedList();
        return getModuleClassLoader(module, urls);
    }

    @NotNull
    public static URLClassLoader getModuleClassLoader(Module module, List<URL> urls) throws MalformedURLException {
        VirtualFile[] classessRoots = OrderEnumerator.orderEntries(module).recursively().getClassesRoots();
        for (VirtualFile virtualFile1 : classessRoots) {
            urls.add(addClassPath(virtualFile1));
        }


        return new URLClassLoader(urls.toArray(new URL[urls.size()]), IntellijUtils.class.getClassLoader());
    }

    public static Editor getEditor(AnActionEvent event) {
        return event.getData(PlatformDataKeys.EDITOR);
    }

    public static Project getProject(AnActionEvent event) {
        return event.getData(PlatformDataKeys.PROJECT);
    }

    @NotNull
    public static URL addClassPath(VirtualFile virtualFile1) throws MalformedURLException {
        String url = virtualFile1.getUrl();
        if (url.contains(".jar") && url.startsWith("jar://")) {
            url = url.replaceAll("jar:/", "jar:file:/");
        }
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        //fix windows urls
        url = url.replaceAll("(jar\\:){0,1}(file\\://)([A-Za-z]:.*)", "$1$2/$3");
        return new URL(url);
    }

    public static void addClassPath(CompileContext compileContext, VirtualFile virtualFile, List<URL> urls, Module module) {
        try {
            urls.add(new URL(virtualFile.getUrl() + "/"));
            //Special case, older configuration often map their output dir to module/classes
            urls.add(new URL(virtualFile.getParent().getParent() + "/classes/"));
            urls.add(new URL(compileContext.getModuleOutputDirectoryForTests(module).getUrl() + "/"));
        } catch (MalformedURLException e) {
            IntellijUtils.log.error(e);
            throw new RuntimeException(e);
        }
    }

    public static void moveFileToGeneratedDir(PsiFile file, Project project, Module module) {
        PsiDirectory dir = PsiDirectoryFactory.getInstance(project).createDirectory(module.getModuleFile().getParent());
        PsiDirectory rscDir = dir.findSubdirectory("generated-sources");
        if (rscDir == null) {
            String subDirStr = "target";
            rscDir = getOrCreate(dir, subDirStr);
            rscDir = getOrCreate(rscDir, "generated-sources");
            rscDir = getOrCreate(rscDir, "ts-ng-tinydecorations");
            rscDir = getOrCreate(rscDir, "services");
        }

        PsiFile oldFile = rscDir.findFile(file.getName());
        if (oldFile != null) {
            oldFile.delete();
        }
        rscDir.add(file);
    }

    public static PsiDirectory getOrCreate(PsiDirectory dir, String subDirStr) {
        PsiDirectory findResult = dir.findSubdirectory(subDirStr);
        return findResult != null ? findResult :
                dir.createSubdirectory(subDirStr);
    }

    @NotNull
    public static String getClassNameFromEditor(Project project, Editor editor) {
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        PsiJavaFile javaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(vFile);

        String packageName = javaFile.getPackageName();
        return packageName + "." + javaFile.getName().replaceAll(".java", "");
    }

    public static Module getModuleFromEditor(Project project, Editor editor) {
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        return ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(vFile);
    }

    public static boolean generate(Project project, Module module, String className, URLClassLoader urlClassLoader) throws ClassNotFoundException {
        Class compiledClass = urlClassLoader.loadClass(className);


        List<RestService> restService = SpringJavaRestReflector.reflectRestService(Arrays.asList(compiledClass), true);
        if (restService == null || restService.isEmpty()) {
            Messages.showErrorDialog(project, "No rest code was found in the selected file", "An Error has occurred");
            return false;
        }
        String text = TypescriptRestGenerator.generate(restService);

        String ext = ".ts";
        String fileName = restService.get(0).getServiceName() + ext;

        generateNewTypescriptFile(text, fileName, project, module);
        return true;
    }

    public static void createAndOpen(Project project, VirtualFile folder, String str, String fileNmae) throws IOException {
        VirtualFile generated = folder.createChildData(project, fileNmae);
        generated.setBinaryContent(str.getBytes());
        FileEditorManager.getInstance(project).openFile(generated, true);
    }



    static class ClassHolder {
        public Class hierarchyEndpoint = null;
    }

    public static boolean generateDto(Project project, Module module, String className, URLClassLoader urlClassLoader) throws ClassNotFoundException {
        final Class compiledClass = urlClassLoader.loadClass(className);
        final ClassHolder classHolder = new ClassHolder();

        classHolder.hierarchyEndpoint = compiledClass;
        if (ReflectUtils.hasParent(compiledClass)) {
            Confirm dialog = new Confirm(data -> {

                if (!Strings.isNullOrEmpty(data)) {
                    try {
                        classHolder.hierarchyEndpoint = urlClassLoader.loadClass(data);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                List<GenericClass> dtos = SpringJavaRestReflector.reflectDto(Arrays.asList(compiledClass), classHolder.hierarchyEndpoint);
                if (dtos == null || dtos.isEmpty()) {
                    Messages.showErrorDialog(project, "No rest code was found in the selected file", "An Error has occurred");
                    return false;
                }
                String text = TypescriptDtoGenerator.generate(dtos);

                String ext = ".ts";
                String fileName = dtos.get(0).getName() + ext;

                generateNewTypescriptFile(text, fileName, project, module);
                return true;
            }, null, ReflectUtils.getInheritanceHierarchyAsString(compiledClass));

            SwingUtils.centerOnParent(dialog, true);
            dialog.pack();
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setVisible(true);
        }


        return true;
    }

    public static Optional<VirtualFile> getCurrentlySelectedDir(Project project) {

        FileEditorManager manager = FileEditorManager.getInstance(project);

        VirtualFile files[] = manager.getSelectedFiles();
        if (files == null || files.length == 0) {
            return Optional.empty();
        } else {
            VirtualFile selFile = files[0];
            if (!selFile.isDirectory()) {
                return Optional.ofNullable(selFile.getParent());
            }
            return Optional.ofNullable(files[0]);
        }
    }

    //https://intellij-support.jetbrains.com/hc/en-us/community/posts/115000080064-find-virtual-file-for-relative-path-under-content-roots
    public static List<VirtualFile> findFileByRelativePath(@NotNull Project project, @NotNull String fileRelativePath) {
        String relativePath = fileRelativePath.startsWith("/") ? fileRelativePath : "/" + fileRelativePath;
        Set<FileType> fileTypes = Collections.singleton(FileTypeManager.getInstance().getFileTypeByFileName(relativePath));
        final List<VirtualFile> fileList = new ArrayList<>();
        FileBasedIndex.getInstance().processFilesContainingAllKeys(FileTypeIndex.NAME, fileTypes, GlobalSearchScope.projectScope(project), null, virtualFile -> {
            if (virtualFile.getPath().endsWith(relativePath)) {
                fileList.add(virtualFile);
            }
            return true;
        });
        return fileList;
    }

    public static List<PsiFile> findFirstAnnotatedClass(Project project, VirtualFile currentDir, String annotationType) {
        List<PsiFile> foundFiles = findAnnotatedFiles(project, currentDir, annotationType);
        while(foundFiles.isEmpty() && project.getBaseDir().getPath().length() <  currentDir.getPath().length()) {
            currentDir = currentDir.getParent();
            foundFiles = findAnnotatedFiles(project, currentDir, annotationType);
        }
        return foundFiles;
    }

    private static List<PsiFile> findAnnotatedFiles(Project project, VirtualFile currentDir, String annotationType) {
        return Arrays.asList(currentDir.getChildren()).parallelStream()
                .filter(vFile -> vFile.getFileType().getDefaultExtension().equalsIgnoreCase("ts"))
                .map(virtualFile -> {
                    return PsiManager.getInstance(project).findFile(virtualFile);
                }).filter(psiFile -> {
                            return IntellijRefactor.hasAnnotatedElement(psiFile, annotationType);
                        }
                ).collect(Collectors.toList());
    }
}
