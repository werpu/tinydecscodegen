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
package net.werpu.tools.supportive.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DocumentContentImpl;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.containers.Convertor;
import net.werpu.tools.actions_all.shared.FileNameTransformer;
import net.werpu.tools.actions_all.shared.SimpleFileNameTransformer;
import net.werpu.tools.configuration.ConfigSerializer;
import net.werpu.tools.gui.Confirm;
import net.werpu.tools.supportive.dtos.ArtifactType;
import net.werpu.tools.supportive.dtos.ModuleElementScope;
import net.werpu.tools.supportive.fs.common.IAngularFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiRouteContext;
import net.werpu.tools.supportive.reflectRefact.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reflector.SpringJavaRestReflector;
import reflector.utils.ReflectUtils;
import rest.GenericClass;
import rest.RestService;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static net.werpu.tools.supportive.utils.IntellijRunUtils.backgroundTask;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.runWithProcessAsnyc;
import static net.werpu.tools.supportive.utils.StringUtils.normalizePath;

/**
 * net.werpu.tools.supportive class to simplify some of the intelli openapi apis
 */
public class IntellijUtils {

    private static final Logger log = Logger.getInstance(IntellijUtils.class);
    public static final String NPM_INSTALL_CONSOLE = "NPM Install Console";

    public static FileNameTransformer fileNameTransformer = new SimpleFileNameTransformer();

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
    protected static void generateOrDiffTsFile(String text, String fileName, String className, Project project, Module module, PsiFile javaFile, ArtifactType artifactType) {
        text = text.replaceAll("\\r", "");
        final Collection<PsiFile> alreadyExisting = Arrays.stream(((PsiJavaFile) javaFile).getClasses()).flatMap(psiJavaClass -> IntellijUtils.searchRefs(project, psiJavaClass.getQualifiedName(), "ts").stream()).collect(Collectors.toList());

        PsiFile file = createRamFileFromText(project,fileName, text, Language.findLanguageByID("TypeScript"));
        ApplicationManager.getApplication().runWriteAction(() -> {
            moveFileToGeneratedDir(file, project, module);
            boolean diffed = false;
            if (alreadyExisting != null && alreadyExisting.size() > 0) {

                for (PsiFile origFile : alreadyExisting) {

                    diffed = diffed || showDiff(project,  origFile,  file, alreadyExisting.size() == 1);
                }
            }
            if (!diffed) {
                writeTarget(className, project, module, artifactType, file);
            }
        });
    }

    private static void writeTarget(String className, Project project, Module module, ArtifactType artifactType, PsiFile file) {
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
            final VirtualFile targetDir = vfile1;

            //todo ng2 filename handling
            if (targetDir != null) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    PropertiesComponent.getInstance(project).setValue("__lastSelTarget__" + artifactType.name(), targetDir.getPath());
                    PsiDirectory dir = PsiDirectoryFactory.getInstance(project).createDirectory(targetDir);
                    dir.add(file);

                    FileEditorManager.getInstance(project).openFile(dir.findFile(file.getName()).getVirtualFile(), true);
                    if (artifactType.isService()) {
                        IntellijFileContext fileContext = new IntellijFileContext(project, dir.findFile(file.getName()).getVirtualFile());

                        try {
                            IntellijRefactor.appendDeclarationToModule(fileContext, ModuleElementScope.PROVIDERS, className, file.getName());
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

    /**
     * Shows a three panel diff
     *
     * @param project  the hosting project
     * @param file     the newly generated file
     * @param origFile the original file
     */
    public static boolean showDiff(Project project, PsiFile origFile,  PsiFile file, boolean showTemp) {
        //we do not show the diffs of target files
        String title = "Reference already exists";
        return showDiff(project, title, origFile, file, showTemp);
    }

    public static boolean showDiff(Project project, String title, PsiFile origFile, PsiFile file, boolean showTemp) {
        if (!showTemp && origFile.getVirtualFile().getPath().contains("target/generated-sources")) {
            return false;
        }

        SimpleDiffRequest request = new SimpleDiffRequest(
                title,
                new DocumentContentImpl(PsiDocumentManager.getInstance(project).getDocument(origFile)),
                new DocumentContentImpl(PsiDocumentManager.getInstance(project).getDocument(file)),
                //new DocumentContentImpl(PsiDocumentManager.getInstance(project).getDocument(javaFile)),
                "Original File: " + origFile.getVirtualFile().getPath(),
                "Newly Generated File"//,
                /*"Java File: "+javaFile.getVirtualFile().getPath().substring(project.getBasePath().length()*/);


        DiffManager.getInstance().showDiff(project, request);
        return true;
    }


    /**
     * Shows a three panel diff
     *
     * @param project  the hosting project
     * @param file     the newly generated file
     * @param origFile the original file
     */
    public static boolean showDiff(String title, Project project, Document origFile, Document file,  boolean showTemp) {
        //we do not show the diffs of target files

        SimpleDiffRequest request = new SimpleDiffRequest(
                title,
                new DocumentContentImpl(origFile),
                new DocumentContentImpl(file),

                "Original File ",
                "Altered File");


        DiffManager.getInstance().showDiff(project, request);
        return true;
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
     * @return true if a new temp file was generated or just an old one recycled (case of a second generation as temp)
     */
    public static boolean moveFileToGeneratedDir(PsiFile file, Project project, Module module) {
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
        boolean same = oldFile != null && !oldFile.getVirtualFile().getCanonicalPath().equals(file.getVirtualFile().getCanonicalPath());
        if (oldFile == null) {
            rscDir.add(file);
        }

        return !same;

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
        return packageName + "." + javaFile.getName().replaceAll(".java", "")
                .replaceAll(".class", "");
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

    public static boolean generateService(Project project, Module module, PsiJavaFile javaFile, boolean ng) throws ClassNotFoundException {
        final AtomicBoolean retVal = new AtomicBoolean(true);


        Arrays.stream(javaFile.getClasses()).forEach(javaClass -> {
            if (!javaClass.hasModifierProperty(PsiModifier.PUBLIC) || !retVal.get()) {
                return;
            }

            List<PsiClass> toReflect = Arrays.asList(javaClass);
            List<RestService> restService = (IntellijSpringRestReflector.isRestService(toReflect)) ?
                    IntellijSpringRestReflector.reflectRestService(toReflect, true, ConfigSerializer.getInstance().getState().getReturnValueStripLevel()) :
                    IntellijJaxRsReflector.reflectRestService(toReflect, true, ConfigSerializer.getInstance().getState().getReturnValueStripLevel());
            if (restService == null || restService.isEmpty()) {
                Messages.showErrorDialog(project, "No rest code was found in the selected file", "An Error has occurred");
                retVal.set(false);
                return;
            }
            String text = IntellijRestGenerator.generate(project, restService, ng);//TypescriptRestGenerator.generate(restService, ng);


            String fileName = fileNameTransformer.transform(restService.get(0).getServiceName());

            generateOrDiffTsFile(text, fileName, restService.get(0).getServiceName(), project, module, javaFile, ArtifactType.SERVICE);
        });

        return retVal.get();
    }


    public static boolean generateService(Project project, Module module, String className, PsiFile javaFile, URLClassLoader urlClassLoader, boolean ng) throws ClassNotFoundException {
        Class compiledClass = urlClassLoader.loadClass(className);


        List<RestService> restService = SpringJavaRestReflector.reflectRestService(Arrays.asList(compiledClass), true);
        if (restService == null || restService.isEmpty()) {
            Messages.showErrorDialog(project, "No rest code was found in the selected file", "An Error has occurred");
            return false;
        }
        String text = IntellijRestGenerator.generate(project, restService, ng);//TypescriptRestGenerator.generate(restService, ng);

        String fileName = fileNameTransformer.transform(restService.get(0).getServiceName());

        generateOrDiffTsFile(text, fileName, className, project, module, javaFile, ArtifactType.SERVICE);
        return true;
    }

    public static IntellijFileContext createAndOpen(Project project, VirtualFile folder, String str, String fileName) throws IOException {
        VirtualFile generated = create(project, folder, str, fileName);
        FileEditorManager.getInstance(project).openFile(generated, true);
        return new IntellijFileContext(project, generated);
    }

    public static VirtualFile create(Project project, VirtualFile folder, String str, String fileName) throws IOException {
        VirtualFile virtualFile = createTempFile(fileName, str);
        virtualFile.rename(project, fileName);
        virtualFile.move(project, folder);

        return virtualFile;

        //PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(folder.getPath()+"/"+fileName, Language.findLanguageByID("XML"), str);
        //return psiFile.getVirtualFile();
    }

    /**
     * creates a temp file
     *
     * @param fileName
     * @param textContent
     * @return
     * @throws IOException
     */
    public static VirtualFile createTempFile(String fileName, String textContent) throws IOException {
        VirtualFile virtualFile = createTempFile(fileName);
        virtualFile.setBinaryContent(textContent.getBytes(virtualFile.getCharset()));
        return virtualFile;
    }

    public static VirtualFile createTempFile(String fileName) throws IOException {
        File tempFile = File.createTempFile(fileName, ".tmp");
        return LocalFileSystem.getInstance().findFileByIoFile(tempFile);
    }

    public static VirtualFile getFolderOrFile(AnActionEvent event) {
        VirtualFile file = event.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
        return file;
    }

    public static void handleEx(Project prj, IOException e) {
        net.werpu.tools.supportive.utils.IntellijUtils.showErrorDialog(prj, "Error", e.getMessage());
        e.printStackTrace();
        throw new RuntimeException(e);
    }

    public static String calculatePackageName(IntellijFileContext ctx, VirtualFile srcRoot) {
        Path targetPath = Paths.get(ctx.getFolderPath());
        Path srcRootPath = Paths.get(srcRoot.getPath());

        return srcRootPath.relativize(targetPath).toString()
                .replaceAll("[/\\\\]+", ".")
                .replaceAll("^\\.(.*)\\.$", "$1");
    }

    public static void npmInstall(Project project, String projectDir, String doneMessage, String doneTitle) {
        BackgroundableProcessIndicator myProcessIndicator = null;


        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        GeneralCommandLine cmd = new GeneralCommandLine(isWindows ? "npm.cmd" : "npm", "install", "--verbose", "--no-progress");
        cmd.withWorkDirectory(projectDir);
        if (!isWindows) {
            cmd.withEnvironment("PATH", System.getenv("PATH") + ":/usr/local/bin:/usr/bin:~/bin:/bin");
        }

        cmd.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE);


        Process p2 = null;
        try {
            p2 = cmd.createProcess();
        } catch (ExecutionException e) {
            Messages.showErrorDialog(project, e.getMessage(), "Error calling npm install");

            e.printStackTrace();
            return;
        }
        final Process p = p2;
        ConsoleFactory.getInstance(p, project, NPM_INSTALL_CONSOLE);

        final Task.Backgroundable myTask = backgroundTask(project,"calling npm install", (progressIndicator) -> {
            try {
                while (p.isAlive()) {
                    Thread.sleep(1000);
                    if (progressIndicator.isCanceled()) {
                        net.werpu.tools.supportive.utils.IntellijUtils.showInfoMessage("npm install has been cancelled", "");
                        return;
                    }
                }
                net.werpu.tools.supportive.utils.IntellijUtils.showInfoMessage(doneMessage, doneTitle);
            } catch (InterruptedException e) {
                net.werpu.tools.supportive.utils.IntellijUtils.showErrorDialog(project, e.getMessage(), "Error");
                e.printStackTrace();
                p.destroy();
                return;
            } finally {
                p.destroy();
            }
            ProjectManager.getInstance().reloadProject(project);
        });

        runWithProcessAsnyc(myTask, "Running npm install");


    }

    /**
     * a helper to generate a conversion function from a tree node
     * to a searchable string for the Intellij tree search
     *
     * @param tree the tree which needs to be searched
     * @return a conversion function on the current tree from node to searchable string
     *
     */
    public static Convertor<TreePath, String> convertToSearchableString(Tree tree) {
        return (TreePath treePath) -> {

            treePath.getPath();
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            final Object userObject = node.getUserObject();
            TreePath nodePath = new TreePath(node.getPath());
            if (!tree.isExpanded(nodePath)) {
                tree.expandPath(nodePath);
            }

            if (userObject instanceof PsiRouteContext) {
                return ((PsiRouteContext) userObject).getRoute().getRouteVarName();
            } else if(userObject instanceof IAngularFileContext) {
                String resourcePath = ((IAngularFileContext) userObject).getVirtualFile().getPath();
                String projectPath = ((IAngularFileContext) userObject).getPsiFile().getProject().getBasePath();
                Path relPath = Paths.get(projectPath).relativize(Paths.get(resourcePath));
                return  ((IAngularFileContext) userObject).getDisplayName()+ " "+  StringUtils.normalizePath(relPath.toString());
            }
            return null;

        };
    }

    public static boolean isTypescript(FileType fileType) {
        String defaultExtension = fileType.getDefaultExtension();
        if(!defaultExtension.startsWith(".")) {
            defaultExtension = "."+defaultExtension;
        }
        return defaultExtension.equalsIgnoreCase(getTsExtension());
    }

    public static void refresh() {
        try {
            FileDocumentManager.getInstance().saveAllDocuments();
            ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
        } finally {
            ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();
        }
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
                String text = IntellijDtoGenerator.generate(project, dtos);//TypescriptDtoGenerator.generate(dtos);

                String fileName = fileNameTransformer.transform(dtos.get(0).getName());

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
            String text = IntellijDtoGenerator.generate(project, dtos);//TypescriptDtoGenerator.generate(dtos);

            String fileName = fileNameTransformer.transform(dtos.get(0).getName());

            generateOrDiffTsFile(text, fileName, className, project, module, javaFile, ArtifactType.DTO);
        }


        return true;
    }


    public static boolean generateDto(Project project, Module module, PsiJavaFile javaFile) throws ClassNotFoundException {
        final AtomicBoolean retVal = new AtomicBoolean(true);
        Arrays.stream(javaFile.getClasses()).forEach(javaClass -> {
            if (!javaClass.hasModifierProperty(PsiModifier.PUBLIC) || !retVal.get()) {
                return;
            }
            String className = javaClass.getQualifiedName();

            if (javaClass.getSuperClass() != null
                    && !javaClass.getSuperClass().getQualifiedName().equals("java.lang.Object")
                    && !javaClass.getSuperClass().getQualifiedName().equals("java.lang.Enum")) {

                Confirm dialog = new Confirm(data -> {


                    List<GenericClass> dtos = IntellijDtoReflector.reflectDto(Arrays.asList(javaClass), data);
                    if (dtos == null || dtos.isEmpty()) {
                        Messages.showErrorDialog(project, "No rest code was found in the selected file", "An Error has occurred");
                        retVal.set(false);
                        return false;
                    }
                    String text = IntellijDtoGenerator.generate(project, dtos);//TypescriptDtoGenerator.generate(dtos);


                    String fileName = fileNameTransformer.transform(dtos.get(0).getName());
                    generateOrDiffTsFile(text, fileName, className, project, module, javaFile, ArtifactType.DTO);
                    return true;
                }, null, IntellijDtoReflector.getInheritanceHierarchyAsString(javaClass));

                SwingUtils.centerOnParent(dialog, true);
                dialog.pack();
                dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setVisible(true);
            } else {
                List<GenericClass> dtos = IntellijDtoReflector.reflectDto(Arrays.asList(javaClass), "");
                if (dtos == null || dtos.isEmpty()) {
                    Messages.showErrorDialog(project, "No rest code was found in the selected file", "An Error has occurred");
                    retVal.set(false);
                    return;
                }
                String text = IntellijDtoGenerator.generate(project, dtos);//TypescriptDtoGenerator.generate(dtos);

                String fileName = fileNameTransformer.transform(dtos.get(0).getName());

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

        String searchStr = "@ref: " + refName;
        return searchComments(project, extension, searchStr);
    }

    /**
     * limit the search to files with comments
     *
     * @param project
     * @param extension
     * @param searchStr
     * @return
     */
    public static Collection<PsiFile> searchComments(Project project, String extension, String searchStr) {
        final Collection<PsiFile> foundFiles = Arrays.asList(PsiSearchHelper.SERVICE.getInstance(project).findCommentsContainingIdentifier(searchStr, GlobalSearchScope.everythingScope(project)))
                .stream()
                .filter(item -> item.getContainingFile().getFileType().getDefaultExtension().equals(extension))
                .filter(item -> !normalizePath(item.getContainingFile().getVirtualFile().getPath()).contains("/generated-sources/"))
                .map(item -> item.getContainingFile())
                .collect(Collectors.toSet());
        return foundFiles;
    }


    public static Collection<PsiFile> searchFiles(Project project, String extension, String searchStr) {
        List<PsiFile> foundFiles = Lists.newLinkedList();

        PsiSearchHelper.SERVICE.getInstance(project).processAllFilesWithWord("", GlobalSearchScope.everythingScope(project),

                (psiFile) -> {
                    if (psiFile.getText().contains("@Component")) {
                        foundFiles.add(psiFile);

                    }
                    return false;
                }, true);


        return foundFiles;
    }

    public static void showInfoMessage(String message, String title) {
        ApplicationManager.getApplication().invokeLater(() -> {
            com.intellij.openapi.ui.Messages.showInfoMessage(message, title);
        });
    }

    public static void showErrorDialog(Project project, String message, String title) {
        ApplicationManager.getApplication().invokeLater(() -> {
            com.intellij.openapi.ui.Messages.showErrorDialog(project, message, title);
        });
    }


    public static String getTsExtension() {
        String retVal = FileTypeManager.getInstance().getStdFileType("TypeScript").getDefaultExtension();
        if (!retVal.startsWith(".")) {
            retVal = "." + retVal;
        }
        return retVal;
    }

    public static String getHtmlExtension() {
        String retVal = FileTypeManager.getInstance().getStdFileType("Html").getDefaultExtension();
        if (!retVal.startsWith(".")) {
            retVal = "." + retVal;
        }
        return retVal;
    }

    static String getJavaExtension() {
        String retVal = FileTypeManager.getInstance().getStdFileType("Java").getDefaultExtension();
        if (!retVal.startsWith(".")) {
            retVal = "." + retVal;
        }
        return retVal;
    }

    /*
     * Action helpers to improve readability
     */

    public static List<Object> flattendArr(Object[] items) {
        List<Object> retList = new LinkedList<>();
        for(Object item: items) {
            if(item.getClass().isArray()) {
                retList.addAll(flattendArr((Object[]) item));
            } else {
                retList.add(item);
            }
        }
        return retList;
    }

    /**
     * one of the most important functions
     * this creates a shadow file (mostly in use by the html)
     * The Psi file then later can be stored or whatever...
     *
     * @param project
     * @param name
     * @param text
     * @param language
     * @return
     */
    public static PsiFile createRamFileFromText(Project project, String name, String text, Language language) {
        return PsiFileFactory.getInstance(project).createFileFromText(name,
                language, text);
    }


    public static Language getTypescriptLanguageDef() {
        return LanguageUtil.getFileTypeLanguage(FileTypeManager.getInstance().getStdFileType("TypeScript"));
    }


    public static Optional<Language> getTnDecTemplateLanguageDef() {
        Language lang = getHtmlLanguage();
        Language [] dialects = LanguageUtil.getLanguageDialects(lang);
        Optional<Language> languageVariant = Arrays.asList(dialects).stream().filter(item -> item.getDisplayName().toLowerCase().contains("angularjs")).findFirst();
        return languageVariant;
    }

    public static Language getHtmlLanguage() {
        return LanguageUtil.getFileTypeLanguage(StdFileTypes.HTML);
    }


    public static Optional<Language> getNgTemplateLanguageDef() {
        Language lang = getHtmlLanguage();
        Language [] dialects = LanguageUtil.getLanguageDialects(lang);
        Optional<Language> languageVariant = Arrays.asList(dialects).stream().filter(item -> item.getDisplayName().toLowerCase().contains("angular2")).findFirst();
        return languageVariant;
    }

}
