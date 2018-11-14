package net.werpu.tools.indexes;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import org.jetbrains.annotations.NotNull;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.werpu.tools.indexes.IndexUtils.standardExclusions;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.CONFIG_ANN;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.TN_UIROUTER_MODULE_FOR_ROOT;

public class TNRoutesIndex extends ScalarIndexExtension<String> {

    public static final ID<String, Void> NAME = ID.create("TN_MainRoutesIndex");
    private final MyDataIndexer myDataIndexer = new MyDataIndexer();

    public static List<PsiFile> getAllAffectedFiles(Project project, IntellijFileContext angularRoot) {
        return FileBasedIndex.getInstance().getContainingFiles(NAME, TN_UIROUTER_MODULE_FOR_ROOT,
                GlobalSearchScope.projectScope(project)).stream()
                .filter(VirtualFile::isValid)
                .filter(vFile -> !(new IntellijFileContext(project, vFile).calculateRelPathTo(angularRoot).startsWith("..")))
                .map(vFile -> PsiManager.getInstance(project).findFile(vFile))
                .filter(psiFile -> psiFile != null && psiFile.getText().contains("@meta: rootRouteConfig"))
                .collect(Collectors.toList());
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
        return new DefaultFileTypeSpecificInputFilter(FileTypeManager.getInstance().getStdFileType("TypeScript"));
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
        @Override
        @NotNull
        public Map<String, Void> map(@NotNull final FileContent inputData) {

            String content = inputData.getContentAsText().toString();
            if ((!standardExclusions(inputData)) && content.contains("\"$routeProvider\"")
                    && content.contains("@Config")
                    && new PsiElementContext(inputData.getPsiFile()).$q(CONFIG_ANN).findFirst().isPresent()) {
                return Collections.singletonMap(TN_UIROUTER_MODULE_FOR_ROOT, null);
            }

            return Collections.emptyMap();
        }
    }

}
