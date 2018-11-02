package supportive.fs.common;

import com.intellij.psi.PsiFile;
import com.intellij.util.gist.GistManagerImpl;
import com.intellij.util.gist.PsiFileGist;
import com.intellij.util.io.DataExternalizer;
import org.jetbrains.annotations.NotNull;
import supportive.utils.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.util.io.IOUtil.readUTF;
import static com.intellij.util.io.IOUtil.writeUTF;
import static java.util.stream.Stream.concat;
import static supportive.reflectRefact.PsiWalkFunctions.*;

public class ComponentFileGist {
    //gist cache for the components to speed things up
    private static PsiFileGist<AngularArtifactGist> psiFileGist = null;
    private static AtomicBoolean initialized = new AtomicBoolean(false);

    public static synchronized void init() {

        if(initialized.get()) {
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
    private static  String _findClazzName(PsiFile in) {
        return  _findComponentClassName(in).get();
    }


    private static String _getTagName(PsiFile file) {

        Optional<PsiElementContext> selector = null;
        try {
            selector = _resolveParameters(file).get("selector");
            if (selector.isPresent()) {
                return selector.get().getText();
            }
        } catch (IOException e) {
            //NOOP for now
        }

        return StringUtils.toDash(_findComponentClassName(file).toString());
    }
}
