package net.werpu.tools.indexes;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;

import java.util.List;
import java.util.stream.Collectors;

import static net.werpu.tools.supportive.utils.StringUtils.normalizePath;

public class IndexUtils {
    public static List<PsiFile> resolve(Project project, IntellijFileContext angularRoot, ID<String, Void> indexId, String indexMarker) {
        return FileBasedIndex.getInstance().getContainingFiles(indexId, indexMarker,
                GlobalSearchScope.projectScope(project)).stream()
                .filter(VirtualFile::isValid)
                //only relative to angular root files
                .filter(vFile -> new IntellijFileContext(project, vFile).isChildOf(angularRoot))
                .map(vFile -> PsiManager.getInstance(project).findFile(vFile))
                .filter(psiFile -> psiFile != null)
                .collect(Collectors.toList());
    }

    public static boolean standardExclusions(@NotNull final FileContent inputData) {
        FileType fileType = inputData.getFile().getFileType();
        return standardSimpleFileExclusion(inputData)
                || !IntellijUtils.isTypescript(fileType);
}

    public static boolean standardSimpleFileExclusion(@NotNull FileContent inputData) {
        return inputData.getFile().isDirectory()
                || normalizePath(inputData.getFile().getPath()).contains("/node_modules/");
    }
}
