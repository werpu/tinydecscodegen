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

package net.werpu.tools.supportive.fs.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.vfs.InvalidVirtualFileAccessException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.gist.GistManagerImpl;
import com.intellij.util.gist.PsiFileGist;
import com.intellij.util.io.DataExternalizer;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;
import net.werpu.tools.supportive.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.util.io.IOUtil.readUTF;
import static com.intellij.util.io.IOUtil.writeUTF;
import static java.util.stream.Stream.concat;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.INVERSE;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.PARENT_SEARCH;

/**
 * Gists are  caches
 * which can be used to speed up operations
 * which normally would take a significant time
 * working on the vfs or on the index.
 * They are more lightweight on the index
 * because they only store a subset of the data. But also
 * they only can read data committed.
 * Hence they are ideal for small amounts of data which is read multiple times.
 * On top of that I have added a volatile secondary thread save
 * memory cache for data requested multiple times but which does not
 * have tu survive in the cache anyway.
 * <p>
 * This is my implementation of a gist which stores
 * component data
 */
public class ComponentFileGist {
    //gist cache for the components to speed things up
    private static PsiFileGist<AngularArtifactGist> psiFileGist = null;
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    /**
     * secondary ram only cache
     */
    private static Cache<String, Object> volatileData = CacheBuilder.newBuilder().weakValues()
            .expireAfterAccess(300, TimeUnit.SECONDS)
            .build();

    public static synchronized void init() {

        if (initialized.get()) {
            return;
        }
        synchronized (ComponentFileGist.class) {

            psiFileGist = GistManagerImpl.getInstance().newPsiFileGist("$$TTCOMPComp", 1, new DataExternalizer<AngularArtifactGist>() {
                @Override
                public void save(@NotNull DataOutput out, AngularArtifactGist value) throws IOException {

                    writeUTF(out, value.getArtifactName());
                    writeUTF(out, value.getTagName());
                    writeUTF(out, value.getClassName());
                    writeUTF(out, value.getFilePath());
                    volatileData.invalidateAll();

                }

                @Override
                public AngularArtifactGist read(@NotNull DataInput in) throws IOException {

                    return new AngularArtifactGist(readUTF(in), readUTF(in), readUTF(in), readUTF(in));
                }
            }, (PsiFile in) -> {
                String componentName = _findClazzName(in);
                String componentTagName = _getTagName(in);
                String className = componentName;

                String filePath = in.getVirtualFile().getPath();
                AngularArtifactGist componentGist = new AngularArtifactGist(componentName, componentTagName, className, filePath);
                return componentGist;
            });
            initialized.set(true);
        }

    }

    public static AngularArtifactGist getFileData(@NotNull PsiFile file) {
        try {
            return psiFileGist.getFileData(file);
        } catch (InvalidVirtualFileAccessException ex) {
            //force a refresh

            final GistListener afterPublisher =
                    file.getProject().getMessageBus().syncPublisher(GistListener.FILE_NOT_REACHABLE);
            afterPublisher.fileNotReachable(file.getVirtualFile());
            return new AngularArtifactGist("", "", "", "");
        }

    }

    public static PsiFileGist<AngularArtifactGist> getPsiFileGist() {
        return psiFileGist;
    }

    /**
     * static helpers for the gist data
     *
     * @param file
     * @return
     */
    private static Optional<String> _findComponentClassName(PsiFile file) {
        IntellijFileContext ctx = new IntellijFileContext(file.getProject(), file);

        return concat(ctx.$q(COMPONENT_CLASS), ctx.$q(CONTROLLER_CLASS))
                .map(el -> el.getName())
                .findFirst();

    }

    @NotNull
    private static AssociativeArraySection _resolveParameters(PsiFile file) {
        IntellijFileContext f = new IntellijFileContext(file.getProject(), file);

        return new AssociativeArraySection(f.getProject(), file, concat(f.$q(COMPONENT_ARGS), f.$q(CONTROLLER_ARGS)).findFirst().get().getElement());
    }

    @NotNull
    private static String _findClazzName(PsiFile in) {
        return _findComponentClassName(in).get();
    }

    private static String _getTagName(PsiFile file) {

        Optional<PsiElementContext> selector = null;
        try {
            selector = _resolveParameters(file).get("selector");
            if (selector.isPresent()) {
                return selector.get().getUnquotedText();
            }
        } catch (IOException e) {
            //NOOP for now
        }

        return StringUtils.toDash(_findComponentClassName(file).orElse("????"));
    }

    public static Optional<PsiElement> getTemplate(PsiFile file, PsiElement componentAnn) {
        int hash = file.getVirtualFile().getPath().hashCode();
        String key = hash + "$$TPL_CTX";
        Object data = volatileData.getIfPresent(key);
        return getProp(file, componentAnn, data, JS_PROP_TEMPLATE);

    }

    public static Optional<PsiElement> getTemplateURL(PsiFile file, PsiElement componentAnn) {
        int hash = file.getVirtualFile().getPath().hashCode();
        String key = hash + "$$TPL_CTX";
        Object data = volatileData.getIfPresent(key);
        return getProp(file, componentAnn, data, JS_PROP_TEMPLATE_URL);

    }

    private static Optional<PsiElement> getProp(PsiFile file, PsiElement componentAnn, Object data, String propName) {
        if (data != null) {
            return (Optional<PsiElement>) data;
        } else {
            Optional<PsiElement> template;
            if (componentAnn == null) {
                template = new IntellijFileContext(file.getProject(), file).$q(JS_PROPERTY, TreeQueryEngine.NAME_EQ(propName))
                        .filter(ComponentFileGist::inTemplateHolder)
                        .map(PsiElementContext::getElement)
                        .findFirst();
            } else {
                template = Arrays.stream(componentAnn.getChildren())
                        .map(PsiElementContext::new)
                        .flatMap(ctx -> ctx.$q(JS_PROPERTY, TreeQueryEngine.NAME_EQ(propName)))
                        .filter(ComponentFileGist::inTemplateHolder)
                        .map(PsiElementContext::getElement)
                        .findFirst();
            }

            return template;
        }
    }

    static boolean inTemplateHolder(PsiElement element) {

        return concat(concat(queryContent(element, PARENT_SEARCH(INVERSE(CONTROLLER_ANN))),
                queryContent(element, PARENT_SEARCH(INVERSE(COMPONENT_ANN)))),
                queryContent(element, PARENT_SEARCH(INVERSE(DIRECTIVE_ANN)))).findFirst()
                .isPresent();

    }

    static boolean inTemplateHolder(PsiElementContext element) {
        PsiElement element1 = element.getElement();

        return inTemplateHolder(element1);
    }
}
