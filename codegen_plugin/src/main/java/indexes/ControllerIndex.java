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
import java.util.stream.Collectors;

public class ControllerIndex extends ScalarIndexExtension<String> {

    public static final ID<String, Void> NAME = ID.create("TN_NG_ControllerIndex");
    public static final String COMPONENT = "@Component";
    public static final String CONTROLLER = "@Controller";
    private final MyDataIndexer myDataIndexer = new MyDataIndexer();

    private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
        @Override
        @NotNull
        public Map<String, Void> map(@NotNull final FileContent inputData) {

            if ((inputData.getContentAsText().toString().contains(COMPONENT) &&
                    inputData.getFile().getPath().replaceAll("\\\\","/" ).contains("/pages/"))
                    || inputData.getContentAsText().toString().contains(CONTROLLER)) {


                if(PsiWalkFunctions.walkPsiTree(inputData.getPsiFile(), PsiWalkFunctions::isComponent, true).size() > 0 ||
                        PsiWalkFunctions.walkPsiTree(inputData.getPsiFile(), PsiWalkFunctions::isController, true).size() > 0) {
                    return Collections.singletonMap(CONTROLLER, null);
                }

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
        return new DefaultFileTypeSpecificInputFilter(FileTypeManager.getInstance().getStdFileType("TypeScript"));
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    public static List<PsiFile> getAllControllerFiles(Project project, IntellijFileContext angularRoot) {
        //Todo filter accordinf to the root dir od the subproject
        return FileBasedIndex.getInstance().getContainingFiles(NAME, CONTROLLER,
                GlobalSearchScope.projectScope(project)).stream()
                .filter(VirtualFile::isValid)

                //only relative to angular root files
                .filter(vFile -> !(new IntellijFileContext(project, vFile).calculateRelPathTo(angularRoot).contains("../")))
                .map(vFile -> PsiManager.getInstance(project).findFile(vFile))
                .filter(psiFile -> psiFile != null)
                .collect(Collectors.toList());
    }

}
