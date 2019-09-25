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
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.werpu.tools.indexes.IndexUtils.standardExclusions;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.COMPONENT_ANN;
import static net.werpu.tools.supportive.utils.StringUtils.normalizePath;

public class ComponentIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> NAME = ID.create("TN_NG_ComponentIndex");
    public static final String ANN_MARKER = "@Component";
    private final MyDataIndexer myDataIndexer = new MyDataIndexer();

    public static boolean isComponent(IntellijFileContext ctx) {
        return ctx.getText().contains("@Component") && ctx.queryContent(COMPONENT_ANN).findFirst().isPresent();
    }

    public static List<PsiFile> getAllAffectedFiles(Project project, IntellijFileContext angularRoot) {

        return IndexUtils.resolve(project, angularRoot, NAME, ANN_MARKER);

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

    private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
        @Override
        @NotNull
        public Map<String, Void> map(@NotNull final FileContent inputData) {

            if ((!standardExclusions(inputData)) &&
                    (!normalizePath(inputData.getFile().getPath()).contains("/pages/")) && isComponent(new IntellijFileContext(inputData.getProject(), inputData.getFile()))
            ) {
                return Collections.singletonMap(ANN_MARKER, null);
            }
            return Collections.emptyMap();

        }
    }
}
