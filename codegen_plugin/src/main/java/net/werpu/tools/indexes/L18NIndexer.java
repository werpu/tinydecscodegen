package net.werpu.tools.indexes;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.werpu.tools.indexes.IndexUtils.standardSimpleFileExclusion;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;

/**
 * Index for l18n files in the system
 */
public class L18NIndexer extends ScalarIndexExtension<String> {

    public static final ID<String, Void> NAME = ID.create("L18NIndexer");


    private final MyDataIndexer myDataIndexer = new MyDataIndexer();

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
        return new DefaultFileTypeSpecificInputFilter(FileTypeManager.getInstance().getFileTypeByExtension(".json"));
    }

    @Override
    public boolean dependsOnFileContent() {
        return false;
    }


    public static List<IntellijFileContext> getAllAffectedFiles(Project project) {
        return FileBasedIndex.getInstance().getContainingFiles(NAME, NAME.getName(),
                GlobalSearchScope.projectScope(project)).stream()
                .filter(VirtualFile::isValid)
                .map(virtualFile -> new IntellijFileContext(project, virtualFile))
                .collect(Collectors.toList());
    }

    private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {


        @Override
        @NotNull
        public Map<String, Void> map(@NotNull final FileContent inputData) {

            if ((!standardSimpleFileExclusion(inputData)) &&
                    inputData.getFile().getName().endsWith(".json") &&
                    (!inputData.getFile().getName().endsWith(NPM_ROOT)) &&
                    (!inputData.getFile().getName().endsWith("bower.json")) &&
                    (!inputData.getFile().getName().startsWith(".")) &&
                    (!inputData.getFile().getName().toLowerCase().endsWith(TS_CONFIG.toLowerCase())) &&
                    (!inputData.getFile().getName().toLowerCase().endsWith(PACKAGE_LOCK.toLowerCase()))
            ) {
                return Collections.singletonMap(NAME.getName(), null);
            }

            return Collections.emptyMap();
        }
    }
}
