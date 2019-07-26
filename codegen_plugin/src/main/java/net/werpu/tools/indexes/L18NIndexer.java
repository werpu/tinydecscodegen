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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.werpu.tools.actions_all.MarkAsI18NTSFile.I18N_MARKER;
import static net.werpu.tools.indexes.IndexUtils.standardSimpleFileExclusion;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.PACKAGE_LOCK;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.TS_CONFIG;

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
                .filter(ctx -> !ctx.isBuildTarget())
                .collect(Collectors.toList());
    }

    private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {


        @Override
        @NotNull
        public Map<String, Void> map(@NotNull final FileContent inputData) {

            String fileName = inputData.getFile().getName().toLowerCase();
            if (
                    (!standardSimpleFileExclusion(inputData)) &&
                            //we index atm json ans typescript files
                            isAllowedFileType(fileName) &&
                            (!fileName.endsWith("bower.json")) &&
                            (!fileName.endsWith("tsconfig.json")) &&
                            (!fileName.startsWith(".")) &&
                            (!fileName.endsWith(TS_CONFIG.toLowerCase())) &&
                            (!fileName.endsWith(PACKAGE_LOCK.toLowerCase())
                                    && isMarked(inputData)

                            )
            ) {
                return Collections.singletonMap(NAME.getName(), null);
            }

            return Collections.emptyMap();
        }
    }

    //atm we only support json and ts filetypes depending on the library support
    //we might extend this in the future
    public static boolean isAllowedFileType(String fileName) {
        return fileName.endsWith(".json") || fileName.endsWith(".ts");
    }

    private static boolean isMarked(@NotNull FileContent inputData) {
        try {


            CharSequence contentAsText = inputData.getContentAsText();
            final StringBuilder sb = new StringBuilder(contentAsText.length());
            sb.append(contentAsText);
            return sb.toString().contains(I18N_MARKER);

        } catch (Throwable e) {
            //fallback because the current intellij version throws an exception with getContentAs text on the indexer side
            //might be a bug
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputData.getFile().getInputStream()));) {
                String result = bufferedReader
                        .lines().collect(Collectors.joining("\n"));
                return result.contains(I18N_MARKER);
            } catch (IOException ex) {
                //well in this case we just return false
            }
            return false;
        }
    }
}
