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
import org.jetbrains.annotations.NotNull;
import net.werpu.tools.supportive.fs.common.AngularVersion;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.werpu.tools.indexes.IndexUtils.standardSimpleFileExclusion;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;

/**
 * indexing the position of all angular subprojects
 */
public class AngularIndex extends ScalarIndexExtension<String> {

    public static final ID<String, Void> NAME = ID.create("TNNG_AngularIndex");
    public static final String TN_MARKER = "\"@types/angular\"";
    public static final String NG_MARKER = "\"@angular/core\"";

    private final MyDataIndexer myDataIndexer = new MyDataIndexer();

    public static boolean isAngularVersion(IntellijFileContext file, AngularVersion angularVersion) {
        List<IntellijFileContext> angularRoots = getAllAffectedRoots(file.getProject(), angularVersion);
        if (angularRoots.isEmpty()) {
            return false;
        }
        return true;
        //return angularRoots.stream().filter(angularRoot -> angularRoot.isBelow(file)).findFirst().isPresent();
    }

    public static boolean isBelowAngularVersion(IntellijFileContext file, AngularVersion angularVersion) {
        List<IntellijFileContext> angularRoots = getAllAffectedRoots(file.getProject(), angularVersion);
        if (angularRoots.isEmpty()) {
            return false;
        }

        return angularRoots.stream().filter(angularRoot -> angularRoot.isBelow(file)).findFirst().isPresent();
    }

    public static List<IntellijFileContext> getAllAffectedRoots(Project project, AngularVersion angularVersion) {

        return FileBasedIndex.getInstance().getContainingFiles(NAME, NPM_ROOT,
                GlobalSearchScope.projectScope(project)).stream()
                .filter(VirtualFile::isValid)
                .map(vFile -> PsiManager.getInstance(project).findFile(vFile))
                .filter(psiFile -> isAngularVersion(angularVersion, psiFile))
                .map(psiFile -> psiFile.getParent())
                .map(psiDirectory -> new IntellijFileContext(project, psiDirectory.getVirtualFile()))
                .collect(Collectors.toList());
    }

    public static boolean isAngularVersion(AngularVersion angularVersion, PsiFile psiFile) {
        String fileName = psiFile != null ? psiFile.getName() : null;
        return psiFile != null &&
                    ((angularVersion == AngularVersion.NG ? fileName.equals(NG_PRJ_MARKER) : false)
                ||  (angularVersion == AngularVersion.TN_DEC ? fileName.equals(TN_DEC_PRJ_MARKER) : false)
                ||  psiFile.getText().contains((angularVersion == AngularVersion.NG ? NG_MARKER : TN_MARKER)));
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

        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull VirtualFile file) {
                return (!file.getPath().contains("node_modules")) && file.getName().equals("package.json") || file.getName().equals(TN_DEC_PRJ_MARKER) || file.getName().equals(NG_PRJ_MARKER);
            }
        };

    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {


        @Override
        @NotNull
        public Map<String, Void> map(@NotNull final FileContent inputData) {
            String fileName = inputData.getFile().getName();

            if(inputData.getFile().getPath().contains("node_modules")) {
                return Collections.emptyMap();
            }
            if (    (!standardSimpleFileExclusion(inputData)) && (
                    isMarkerFile(fileName) || isNpmProjectDef(inputData, fileName)))  {
                return Collections.singletonMap(NPM_ROOT, null);
            }

            return Collections.emptyMap();
        }

        public boolean isNpmProjectDef(@NotNull FileContent inputData, String fileName) {
            return fileName.equals(NPM_ROOT)
            &&
                    isEmbbededInPackageJson(inputData);
        }

        public boolean isEmbbededInPackageJson(@NotNull FileContent inputData) {
            return inputData.getPsiFile().getText().contains(NG_MARKER) ||
                    inputData.getPsiFile().getText().contains(TN_MARKER);
        }

        public static boolean isMarkerFile(String fileName) {
            return fileName.equals(TN_DEC_PRJ_MARKER) || fileName.equals(NG_PRJ_MARKER);
        }
    }


}
