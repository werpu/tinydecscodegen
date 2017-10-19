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
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DocumentContentImpl;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingRegistry;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import gui.Confirm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reflector.SpringJavaRestReflector;
import reflector.TypescriptDtoGenerator;
import reflector.TypescriptRestGenerator;
import reflector.utils.ReflectUtils;
import rest.GenericClass;
import rest.RestService;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * utils class to simplify some of the intelli openapi apis
 */
public class IntellijUtils {

    private static final Logger log = Logger.getInstance(IntellijUtils.class);


    /**
     * Generates or diffs a typescript file.
     * It searches for refs and if some are found a diff editor is opened
     * otherwise a typescript file is generated and the user can chose to drop it into the filesystem
     *
     * @param text      the text to process as file
     * @param fileName  the file name
     * @param className the classname of the originating class
     * @param project   the project
     * @param module    the module
     * @param javaFile  the originating java file
     */
    public static void generateOrDiffTsFile(String text, String fileName, String className, Project project, Module module, PsiFile javaFile, ArtifactType artifactType) {
        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(fileName, Language.findLanguageByID("TypeScript"), text);
        final Collection<PsiFile> alreadyExisting = IntellijUtils.searchRefs(project, className, "ts");
        ApplicationManager.getApplication().runWriteAction(() -> {
            moveFileToGeneratedDir(file, project, module);
            if (alreadyExisting != null && alreadyExisting.size() > 0) {
                for (PsiFile origFile : alreadyExisting) {

                    showDiff(project, file, origFile, javaFile);
                }
            } else {
                ApplicationManager.getApplication().invokeLater(() -> {

                    String oldPath = PropertiesComponent.getInstance(project).getValue("__lastSelTarget__" + artifactType.name());
                    VirtualFile vfile1 = null;
                    FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
                    descriptor.setTitle("Select Generation Target Directory");
                    descriptor.setDescription("Please choose a target directory");


                    if (!Strings.isNullOrEmpty(oldPath)) {
                        VirtualFile storedPath = LocalFileSystem.getInstance().findFileByPath(oldPath);
                        vfile1 = FileChooser.chooseFile(descriptor, project, storedPath);
                    } else {
                        vfile1 = FileChooser.chooseFile(descriptor, project, module.getModuleFile());
                    }
                    final VirtualFile vfile = vfile1;

                    if (vfile != null) {
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            PropertiesComponent.getInstance(project).setValue("__lastSelTarget__" + artifactType.name(), vfile.getPath());
                            PsiDirectory dir = PsiDirectoryFactory.getInstance(project).createDirectory(vfile);
                            dir.add(file);

                            FileEditorManager.getInstance(project).openFile(dir.findFile(file.getName()).getVirtualFile(), true);
                            if (artifactType.isService()) {
                                IntellijFileContext fileContext = new IntellijFileContext(project, dir.getVirtualFile());

                                try {
                                    IntellijRefactor.appendDeclarationToModule(fileContext, ModuleElementScope.DECLARATIONS, className);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    } else {
                        FileEditorManager.getInstance(project).openFile(file.getVirtualFile(), true);
                    }
                });
            }
        });
    }

    /**
     * Shows a three panel diff
     *
     * @param project  the hosting project
     * @param file     the newly generated file
     * @param origFile the original file
     * @param javaFile the root java file for the diff
     */
    public static void showDiff(Project project, PsiFile file, PsiFile origFile, PsiFile javaFile) {
        //we do not show the diffs of target files
        if (origFile.getVirtualFile().getPath().contains("target/generated-sources")) {
            return;
        }
        SimpleDiffRequest request = new SimpleDiffRequest(
                "Reference already exists",
                new DocumentContentImpl(PsiDocumentManager.getInstance(project).getDocument(origFile)),
                new DocumentContentImpl(PsiDocumentManager.getInstance(project).getDocument(file)),
                //new DocumentContentImpl(PsiDocumentManager.getInstance(project).getDocument(javaFile)),
                "Original File: " + origFile.getVirtualFile().getPath().substring(project.getBasePath().length()),
                "Newly Generated File"//,
                /*"Java File: "+javaFile.getVirtualFile().getPath().substring(project.getBasePath().length()*/);


        DiffManager.getInstance().showDiff(project, request);
    }

    /**
     * fetches the classloader of a specified compile context
     *
     * @param compileContext
     * @param module
     * @return
     * @throws MalformedURLException
     */
    @NotNull
    public static URLClassLoader getClassLoader(CompileContext compileContext, Module module) throws MalformedURLException {
        VirtualFile virtualFile = compileContext.getModuleOutputDirectoryForTests(module);

        List<URL> urls = Lists.newLinkedList();
        addClassPath(compileContext, virtualFile, urls, module);

        return getModuleClassLoader(module, urls);
    }

    /**
     * fetches the classloadert of a specified module
     *
     * @param module
     * @return
     * @throws MalformedURLException
     */
    @NotNull
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

    /**
     * gets an editor for a specified event
     *
     * @param event
     * @return
     */
    @Nullable
    public static Editor getEditor(AnActionEvent event) {
        return event.getData(PlatformDataKeys.EDITOR);
    }

    /**
     * gets the project for a specified event
     *
     * @param event
     * @return
     */
    @NotNull
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

    /**
     * ad as few classpath params which might not be generated by the auto classpath functionality
     * of the compile context
     *
     * @param compileContext
     * @param virtualFile
     * @param urls
     * @param module
     */
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

    /**
     * move a psi file to the neutral position for internal storage
     *
     * @param file
     * @param project
     * @param module
     */
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

    /**
     * create a directory if not existent
     *
     * @param dir
     * @param subDirStr
     * @return
     */
    public static PsiDirectory getOrCreate(PsiDirectory dir, String subDirStr) {
        PsiDirectory findResult = dir.findSubdirectory(subDirStr);
        return findResult != null ? findResult :
                dir.createSubdirectory(subDirStr);
    }

    @NotNull
    /**
     * fetches the class name of a hosted java class from a given editor
     */
    public static String getClassNameFromEditor(Project project, Editor editor) {
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        PsiJavaFile javaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(vFile);

        String packageName = javaFile.getPackageName();
        return packageName + "." + javaFile.getName().replaceAll(".java", "");
    }

    /**
     * fetches the module from the given editor
     *
     * @param project
     * @param editor
     * @return
     */
    public static Module getModuleFromEditor(Project project, Editor editor) {
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        return ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(vFile);
    }

    public static boolean generateService(Project project, Module module, String className, PsiFile javaFile, URLClassLoader urlClassLoader) throws ClassNotFoundException {
        Class compiledClass = urlClassLoader.loadClass(className);


        List<RestService> restService = SpringJavaRestReflector.reflectRestService(Arrays.asList(compiledClass), true);
        if (restService == null || restService.isEmpty()) {
            Messages.showErrorDialog(project, "No rest code was found in the selected file", "An Error has occurred");
            return false;
        }
        String text = TypescriptRestGenerator.generate(restService);

        String ext = ".ts";
        String fileName = restService.get(0).getServiceName() + ext;

        generateOrDiffTsFile(text, fileName, className, project, module, javaFile, ArtifactType.SERVICE);
        return true;
    }

    public static void createAndOpen(Project project, VirtualFile folder, String str, String fileNmae) throws IOException {
        VirtualFile generated = folder.createChildData(project, fileNmae);
        generated.setBinaryContent(str.getBytes(generated.getCharset()));
        FileEditorManager.getInstance(project).openFile(generated, true);
    }

    //https://stackoverflow.com/questions/1086123/string-conversion-to-title-case
    public static String toCamelCase(String s) {

        final String ACTIONABLE_DELIMITERS = " '-/\\."; // these cause the character following
        // to be capitalized

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;

        for (char c : s.toCharArray()) {
            c = (capNext)
                    ? Character.toUpperCase(c)
                    : Character.toLowerCase(c);
            sb.append(c);
            capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
        }
        return sb.toString().replaceAll("[-/\\.]", "");
    }

    public static VirtualFile getFolderOrFile(AnActionEvent event) {
        VirtualFile file = event.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
        return file;
    }


    static class ClassHolder {
        public Class hierarchyEndpoint = null;
    }

    public static boolean generateDto(Project project, Module module, String className, PsiFile javaFile, URLClassLoader urlClassLoader) throws ClassNotFoundException {
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

                generateOrDiffTsFile(text, fileName, className, project, module, javaFile, ArtifactType.DTO);
                return true;
            }, null, ReflectUtils.getInheritanceHierarchyAsString(compiledClass));

            SwingUtils.centerOnParent(dialog, true);
            dialog.pack();
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setVisible(true);
        } else {
            List<GenericClass> dtos = SpringJavaRestReflector.reflectDto(Arrays.asList(compiledClass), compiledClass);
            if (dtos == null || dtos.isEmpty()) {
                Messages.showErrorDialog(project, "No rest code was found in the selected file", "An Error has occurred");
                return false;
            }
            String text = TypescriptDtoGenerator.generate(dtos);

            String ext = ".ts";
            String fileName = dtos.get(0).getName() + ext;

            generateOrDiffTsFile(text, fileName, className, project, module, javaFile, ArtifactType.DTO);
        }


        return true;
    }


    public static boolean generateDto(Project project, Module module, PsiJavaFile javaFile) throws ClassNotFoundException {
        final AtomicBoolean retVal = new AtomicBoolean(true);
        Arrays.stream(javaFile.getClasses()).forEach(javaClass -> {
            if(!javaClass.hasModifierProperty(PsiModifier.PUBLIC) || !retVal.get()) {
                return;
            }
            String className = javaClass.getQualifiedName();

            if(javaClass.getSuperClass() != null) {
                Confirm dialog = new Confirm(data -> {


                    List<GenericClass> dtos = IntellijSpringJavaRestReflector.reflectDto(Arrays.asList(javaClass), data);
                    if (dtos == null || dtos.isEmpty()) {
                        Messages.showErrorDialog(project, "No rest code was found in the selected file", "An Error has occurred");
                        retVal.set(false);
                        return false;
                    }
                    String text = TypescriptDtoGenerator.generate(dtos);

                    String ext = ".ts";
                    String fileName = dtos.get(0).getName() + ext;


                    generateOrDiffTsFile(text, fileName, className, project, module, javaFile, ArtifactType.DTO);
                    return true;
                }, null, IntellijSpringJavaRestReflector.getInheritanceHierarchyAsString(javaClass));

                SwingUtils.centerOnParent(dialog, true);
                dialog.pack();
                dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setVisible(true);
            } else {
                List<GenericClass> dtos = IntellijSpringJavaRestReflector.reflectDto(Arrays.asList(javaClass), "");
                if (dtos == null || dtos.isEmpty()) {
                    Messages.showErrorDialog(project, "No rest code was found in the selected file", "An Error has occurred");
                    retVal.set(false);
                    return;
                }
                String text = TypescriptDtoGenerator.generate(dtos);

                String ext = ".ts";
                String fileName = dtos.get(0).getName() + ext;

                generateOrDiffTsFile(text, fileName, className, project, module, javaFile, ArtifactType.DTO);
            }
        });


        return retVal.get();
    }


    /**
     * search in the comments of a given filetype for refs
     *
     * @param project   the project in wich to search
     * @param refName   the unique ref name
     * @param extension the filetype extension
     * @return a set of found files
     */
    @NotNull
    public static Collection<PsiFile> searchRefs(Project project, String refName, String extension) {

        final Collection<PsiFile> foundFiles = Arrays.asList(PsiSearchHelper.SERVICE.getInstance(project).findCommentsContainingIdentifier("@ref: " + refName, GlobalSearchScope.everythingScope(project)))
                .stream()
                .filter(item -> item.getContainingFile().getFileType().getDefaultExtension().equalsIgnoreCase(extension))
                .map(item -> item.getContainingFile())
                .collect(Collectors.toSet());
        return foundFiles;
    }
}
