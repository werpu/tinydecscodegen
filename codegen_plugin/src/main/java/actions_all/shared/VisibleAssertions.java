package actions_all.shared;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import supportive.fs.common.AngularVersion;
import supportive.fs.common.IntellijFileContext;

import java.util.Optional;

public class VisibleAssertions {
    public static boolean assertNotJavaRest(IntellijFileContext ctx) {
        return ctx.getProject() == null ||
                ctx.getPsiFile() == null ||
                ctx.getPsiFile().getVirtualFile() == null ||
                !ctx.getPsiFile().getVirtualFile().getPath().endsWith(".java") ||
                ctx.getDocument().getText().indexOf("@Path") == -1;
    }

    public static boolean assertNotSpringRest(IntellijFileContext ctx) {
        return ctx.getDocument() == null || ctx.getDocument().getText().indexOf("@RestController") == -1;
    }

    public static boolean assertNotJava(IntellijFileContext ctx) {
        return ctx.getPsiFile() == null ||
                !ctx.getPsiFile().getVirtualFile().getPath().endsWith(".java");
    }


    public static boolean assertNotRef(IntellijFileContext ctx) {
        return ctx.getPsiFile() == null ||
                !ctx.getPsiFile().getText().contains("@ref:");
    }


    public static boolean assertNotTs(IntellijFileContext ctx) {
        return ctx.getPsiFile() == null || !ctx.getPsiFile().getVirtualFile().getPath().endsWith(".ts");
    }

    public static boolean assertTemplated(IntellijFileContext ctx) {
        return ctx.getDocument().getText().contains("@Component") ||
                ctx.getDocument().getText().contains("@Directive") ||
                ctx.getDocument().getText().contains("@Controller");
    }

    public static void tnVisible(AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        Optional<AngularVersion> angularVersion = ctx.getAngularVersion();
        if (!angularVersion.isPresent() || !angularVersion.get().equals(AngularVersion.TN_DEC)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }

    public static void ngVisible(AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        Optional<AngularVersion> angularVersion = ctx.getAngularVersion();
        if (!angularVersion.isPresent() || !angularVersion.get().equals(AngularVersion.NG)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }

    public static void tnNoProject(AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        Optional<AngularVersion> angularVersion = ctx.getAngularVersion();
        if (angularVersion.isPresent()) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }

    public static void tsOnlyVisible(AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);

        Optional<AngularVersion> angularVersion = ctx.getAngularVersion();
        if (assertNotTs(ctx) || !angularVersion.isPresent()) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }


    public static boolean refOnlyVisible(AnActionEvent anActionEvent) {
        if(assertNotRef(new IntellijFileContext(anActionEvent))) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return true;
        }
        return false;
    }
}
