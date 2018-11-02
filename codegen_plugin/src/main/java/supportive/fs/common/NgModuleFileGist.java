package supportive.fs.common;

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
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.util.io.IOUtil.readUTF;
import static com.intellij.util.io.IOUtil.writeUTF;
import static java.util.stream.Stream.concat;
import static supportive.reflectRefact.PsiWalkFunctions.*;

//TODO introduce a secondary cache for volatile data like a param section
//which cannot be stored
public class NgModuleFileGist {
    //gist cache for the components to speed things up
    private static PsiFileGist<AngularArtifactGist> psiFileGist = null;
    private static AtomicBoolean initialized = new AtomicBoolean(false);

    private static Map<String, Object> volatileData = new ConcurrentHashMap<>();

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
                    volatileData.clear();

                }

                @Override
                public AngularArtifactGist read(@NotNull DataInput in) throws IOException {

                    return new AngularArtifactGist(readUTF(in), readUTF(in), readUTF(in), readUTF(in));
                }
            }, (PsiFile in) -> {
                String componentName = _findModuleName(in);
                String componentTagName = "";
                String className = componentName;

                String filePath = in.getVirtualFile().getPath();
                AngularArtifactGist retVal = new AngularArtifactGist(componentName, componentTagName, className, filePath);
                return retVal;
            });
            initialized.set(true);
        }

    }

    public static AngularArtifactGist getFileData(@NotNull PsiFile file) {
        return psiFileGist.getFileData(file);
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
        if(volatileData.containsKey(key)) {
            return (PsiElementContext) volatileData.get(key);
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
        if(volatileData.containsKey(key)) {
            return (AssociativeArraySection) volatileData.get(key);
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
