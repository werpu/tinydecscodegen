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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static indexes.IndexUtils.standardExclusions;
import static supportive.reflectRefact.PsiWalkFunctions.COMPONENT_ANN;

public class ComponentIndex extends ScalarIndexExtension<String> {

    public static final ID<String, Void> NAME = ID.create("TN_NG_ComponentIndex");
    public static final String ANN_MARKER = "@Component";
    private final MyDataIndexer myDataIndexer = new MyDataIndexer();

    private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
        @Override
        @NotNull
        public Map<String, Void> map(@NotNull final FileContent inputData) {

            if ((!standardExclusions(inputData)) && isComponent(new IntellijFileContext(inputData.getProject(), inputData.getFile()))) {
                return Collections.singletonMap(ANN_MARKER, null);
            }
            return Collections.emptyMap();


        }
    }

    public static boolean isComponent(IntellijFileContext ctx) {
        return ctx.getText().contains("@Component") && ctx.queryContent(COMPONENT_ANN).findFirst().isPresent();
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

    public static List<PsiFile> getAllAffectedFiles(Project project, IntellijFileContext angularRoot) {

        return IndexUtils.resolve(project, angularRoot, NAME, ANN_MARKER);


    }
}
