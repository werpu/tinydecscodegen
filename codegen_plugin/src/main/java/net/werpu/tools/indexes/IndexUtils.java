/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

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
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;

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
