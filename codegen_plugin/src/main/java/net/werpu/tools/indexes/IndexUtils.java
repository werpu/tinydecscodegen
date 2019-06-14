package net.werpu.tools.indexes;

import com.intellij.javaee.web.WebUtil;
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

    //https://intellij-support.jetbrains.com/hc/en-us/community/posts/360003850460-How-to-detect-whether-a-file-is-part-of-a-webapp-resource-dir
    //we could also go for the WebHelpers solution but that way we might break
    public static boolean standardSimpleFileExclusion(@NotNull FileContent inputData) {
        String normalizePath = normalizePath(inputData.getFile().getPath());
        return inputData.getFile().isDirectory()
                || normalizePath.contains("/node_modules/")
                || normalizePath.contains("/target/")
                || normalizePath.contains("/out/")
                || normalizePath.contains("/build/");
    }
}
