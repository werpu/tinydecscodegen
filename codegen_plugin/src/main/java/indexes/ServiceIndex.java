package indexes;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import supportive.fs.common.IntellijFileContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static indexes.IndexUtils.standardExclusions;
import static supportive.reflectRefact.PsiWalkFunctions.SERVICE_ANN;

public class ServiceIndex  extends ScalarIndexExtension<String> {

    public static final ID<String, Void> NAME = ID.create("TN_NG_ServiceIndex");
    public static final String ANNOTATION_MARKER = "@Injectable";
    private final ServiceIndex.MyDataIndexer myDataIndexer = new ServiceIndex.MyDataIndexer();

    private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
        @Override
        @NotNull
        public Map<String, Void> map(@NotNull final FileContent inputData) {

            if ((!standardExclusions(inputData)) && isMarked(new IntellijFileContext(inputData.getProject(), inputData.getFile()))) {
                return Collections.singletonMap(ANNOTATION_MARKER, null);
            }
            return Collections.emptyMap();


        }
    }

    public static boolean isMarked(IntellijFileContext ctx) {
        return ctx.getText().contains("@Injectable") && ctx.queryContent(SERVICE_ANN).findFirst().isPresent();
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
        return IndexUtils.resolve(project, angularRoot, NAME, ANNOTATION_MARKER);
    }
}
