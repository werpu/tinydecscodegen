package supportive.fs.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.vfs.InvalidVirtualFileAccessException;
import com.intellij.psi.PsiFile;
import com.intellij.util.gist.GistManagerImpl;
import com.intellij.util.gist.PsiFileGist;
import com.intellij.util.io.DataExternalizer;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.util.io.IOUtil.readUTF;
import static com.intellij.util.io.IOUtil.writeUTF;
import static java.util.stream.Stream.concat;
import static supportive.reflectRefact.PsiWalkFunctions.*;

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
 *
 * This is my implementation of a gist which stores
 * module file data
 *
 */
public class NgModuleFileGist {
    //gist cache for the components to speed things up
    private static PsiFileGist<AngularArtifactGist> psiFileGist = null;


    private static AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * secondary ram only cache
     */
    private static Cache<String, Object> volatileData = CacheBuilder.newBuilder()
            .weakValues()
            .expireAfterAccess(300, TimeUnit.SECONDS)
            .build();

    public static synchronized void init() {

        if (initialized.get()) {
            return;
        }
        synchronized (NgModuleFileGist.class) {


            psiFileGist = GistManagerImpl.getInstance().newPsiFileGist("$$TTNGModule", 1, new DataExternalizer<AngularArtifactGist>() {
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
                try {
                    String componentName = _findModuleName(in);
                    String componentTagName = "";
                    String className = componentName;

                    String filePath = in.getVirtualFile().getPath();
                    AngularArtifactGist retVal = new AngularArtifactGist(componentName, componentTagName, className, filePath);
                    return retVal;
                } catch (RuntimeException ex) {
                    return null;
                }
            });
            initialized.set(true);
        }

    }

    public static AngularArtifactGist getFileData(@NotNull PsiFile file) {
        try {
            return psiFileGist.getFileData(file);
        } catch(InvalidVirtualFileAccessException | AssertionError ex /*project not active anymore can happen in case of stale cashes*/) {
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
    private static Optional<PsiElementContext> _resolveClassCtx(PsiFile file) {
        Optional<PsiElementContext> clazz = new PsiElementContext(file).$q(MODULE_CLASS).findFirst();
        if (!clazz.isPresent()) {
            throw new RuntimeException("Module class not found");
        }
        return clazz;

    }

    @NotNull
    public static PsiElementContext getResourceRoot(PsiFile file) {
        int hash = file.getVirtualFile().getPath().hashCode();
        String key = hash + "$$ROOT_CTX";
        Object data = volatileData.getIfPresent(key);
        if(data != null) {
            return (PsiElementContext) data;
        } else {
            PsiElementContext clazz = _resolveClass(file);
            volatileData.put(key, clazz);
            return clazz;
        }
    }

    @NotNull
    public static AssociativeArraySection resolveParameters(PsiFile file) {
        int hash = file.getVirtualFile().getPath().hashCode();
        String key = hash + "$$PARAMS_CTX";
        Object data = volatileData.getIfPresent(key);
        if(data != null) {
            return (AssociativeArraySection) data;
        } else {
            IntellijFileContext ctx = new IntellijFileContext(file.getProject(), file);
            AssociativeArraySection section = new AssociativeArraySection(file.getProject(), file, ctx.$q(MODULE_ARGS).findFirst().get().getElement());
            volatileData.put(key, section);
            return section;
        }

    }


    @NotNull
    private static String _findClazzName(PsiFile in) {
        final ScalarValue <String> retVal = new ScalarValue<>("");
        _resolveClassCtx(in).ifPresent(present -> retVal.setValue(present.getName()));
        return retVal.getValue();
    }

    @NotNull
    private static String _findModuleName(PsiFile in) {
        return _findClazzName(in);
    }

    private static PsiElementContext _resolveClass(PsiFile in) {
        Optional<PsiElementContext> clazz = new PsiElementContext(in).$q(MODULE_CLASS).findFirst();
        if(!clazz.isPresent()) {
            throw new RuntimeException("Module class not found");
        }
        return clazz.get();
    }

}
