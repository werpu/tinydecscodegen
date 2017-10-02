package factories;


import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class TnDecLiveTemplatesContextProvider extends TemplateContextType {
    protected TnDecLiveTemplatesContextProvider() {
        super("TINY_DECS", "Tiny Decorations");
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        return file.getName().endsWith(".md");
    }
}

