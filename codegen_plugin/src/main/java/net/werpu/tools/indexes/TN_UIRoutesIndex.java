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

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.werpu.tools.indexes.IndexUtils.standardExclusions;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.CONFIG_ANN;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.TN_ROUTES_UIROUTER_MODULE_FOR_ROOT;

public class TN_UIRoutesIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> NAME = ID.create("TN_UIRoutesIndex");
    private final TN_UIRoutesIndex.MyDataIndexer myDataIndexer = new TN_UIRoutesIndex.MyDataIndexer();

    public static List<PsiFile> getAllAffectedFiles(Project project, IntellijFileContext angularRoot) {
        return FileBasedIndex.getInstance().getContainingFiles(NAME, TN_ROUTES_UIROUTER_MODULE_FOR_ROOT,
                GlobalSearchScope.projectScope(project)).stream()
                .filter(VirtualFile::isValid)
                .filter(vFile -> !(new IntellijFileContext(project, vFile).calculateRelPathTo(angularRoot).startsWith("..")))
                .map(vFile -> PsiManager.getInstance(project).findFile(vFile))
                //.map(psiFile -> new NG_UIRoutesRoutesFileContext(project, psiFile))
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

            //if()
            String content = inputData.getContentAsText().toString();
            if ((!standardExclusions(inputData)) && content.contains("\"$stateProvider\"")
                    && content.contains("@Config")
                    && new PsiElementContext(inputData.getPsiFile()).$q(CONFIG_ANN).findFirst().isPresent()) {
                return Collections.singletonMap(TN_ROUTES_UIROUTER_MODULE_FOR_ROOT, null);
            }

            return Collections.emptyMap();
        }
    }

}
