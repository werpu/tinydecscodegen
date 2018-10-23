package indexes;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import supportive.fs.common.IntellijFileContext;
import supportive.reflectRefact.PsiWalkFunctions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModuleIndex extends ScalarIndexExtension<String> {

    public static final ID<String, Void> NAME = ID.create("TN_ModuleIndex");
    public static final String MODULE = "@Module";
    private final MyDataIndexer myDataIndexer = new MyDataIndexer();

    private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
        @Override
        @NotNull
        public Map<String, Void> map(@NotNull final FileContent inputData) {

            String text = inputData.getContentAsText().toString();
            if (text.contains(MODULE) ||
                text.contains(".module(") ||
                text.contains(".component(")
            ) {
                return Collections.singletonMap(MODULE, null);
            }
            return Collections.emptyMap();


        }
    }

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return myDataIndexer;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        //TODO debug this
        return new DefaultFileTypeSpecificInputFilter(FileTypeManager.getInstance().getStdFileType("TypeScript"));
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    public static List<PsiFile> getAllModuleFiles(Project project, IntellijFileContext angularRoot) {
        return FileBasedIndex.getInstance().getContainingFiles(NAME, MODULE,
                GlobalSearchScope.projectScope(project)).stream()
                .filter(VirtualFile::isValid)
                //only relative to angular root files
                .filter(vFile -> !(new IntellijFileContext(project, vFile).calculateRelPathTo(angularRoot).startsWith("..")))
                .map(vFile -> PsiManager.getInstance(project).findFile(vFile))
                .filter(psiFile -> psiFile != null)
                //.map(psiFile -> new ComponentFileContext(project, psiFile))
                .collect(Collectors.toList());
    }

    public static Map<String, PsiFile> getAllModuleFilesAsMap(Project project, IntellijFileContext angularRoot) {
        return FileBasedIndex.getInstance().getContainingFiles(NAME, MODULE,
                GlobalSearchScope.projectScope(project)).stream()
                .filter(VirtualFile::isValid)
                //only relative to angular root files
                .filter(vFile -> !(new IntellijFileContext(project, vFile).calculateRelPathTo(angularRoot).startsWith("..")))
                .map(vFile -> PsiManager.getInstance(project).findFile(vFile))
                .filter(psiFile -> psiFile != null)
                //.map(psiFile -> new ComponentFileContext(project, psiFile))
                .collect(Collectors.toMap(psiFile -> psiFile.getVirtualFile().getPath(), psiFile -> psiFile));
    }
}
