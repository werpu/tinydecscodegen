package net.werpu.tools.actions_all.shared;

import com.intellij.openapi.actionSystem.AnActionEvent;
import net.werpu.tools.indexes.AngularIndex;
import net.werpu.tools.supportive.fs.common.AngularVersion;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.IntellijUtils;

import java.util.Optional;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.EL_TEXT_EQ;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.JS_ES_6_DECORATOR;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.PSI_ELEMENT_JS_IDENTIFIER;
import static net.werpu.tools.supportive.utils.IntellijUtils.getTsExtension;

public class VisibleAssertions {
    public static boolean assertNotJavaRest(IntellijFileContext ctx) {
        return ctx.getProject() == null ||
                ctx.getPsiFile() == null ||
                ctx.getPsiFile().getVirtualFile() == null ||
                !ctx.getPsiFile().getVirtualFile().getPath().endsWith(".java") ||
                ctx.getText().indexOf("@Path") == -1;
    }

    public static boolean assertNotSpringRest(IntellijFileContext ctx) {
        return ctx.getDocument() == null || ctx.getText().indexOf("@RestController") == -1;
    }

    public static boolean assertNotJava(IntellijFileContext ctx) {
        return ctx.getPsiFile() == null ||
                (!ctx.getPsiFile().getVirtualFile().getPath().endsWith(".java") &&
                        !ctx.getPsiFile().getVirtualFile().getPath().endsWith(".class"));
    }


    public static boolean assertNotRef(IntellijFileContext ctx) {
        return ctx.getPsiFile() == null ||
                !ctx.getPsiFile().getText().contains("@ref:");
    }


    public static boolean assertNotTs(IntellijFileContext ctx) {
        return ctx.getPsiFile() == null || !ctx.getPsiFile().getVirtualFile().getPath().endsWith(getTsExtension());
    }


    public static boolean assertTemplated(IntellijFileContext ctx) {
        return ctx.getText().contains("@Component") ||
                ctx.getText().contains("@Directive") ||
                ctx.getText().contains("@Controller");
    }

    public static boolean assertController(IntellijFileContext ctx) {
        return ctx.queryContent(JS_ES_6_DECORATOR, PSI_ELEMENT_JS_IDENTIFIER, EL_TEXT_EQ("Controller")).findFirst().isPresent() ||
                ctx.queryContent(JS_ES_6_DECORATOR, PSI_ELEMENT_JS_IDENTIFIER, EL_TEXT_EQ("Component")).findFirst().isPresent();

    }

    public static void tnVisible(AnActionEvent anActionEvent) {
        if (IntellijUtils.getFolderOrFile(anActionEvent) == null) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }


        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        if (!ctx.isAngularChild(AngularVersion.TN_DEC)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }

    public static void ngVisible(AnActionEvent anActionEvent) {
        if (IntellijUtils.getFolderOrFile(anActionEvent) == null) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        if (!ctx.isAngularChild(AngularVersion.NG)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }

    public static void tnNoProject(AnActionEvent anActionEvent) {
        if (IntellijUtils.getFolderOrFile(anActionEvent) == null) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        ;
        if (ctx.isAngularChild()) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }

    public static void tsOnlyVisible(AnActionEvent anActionEvent) {
        if (IntellijUtils.getFolderOrFile(anActionEvent) == null) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);

        Optional<AngularVersion> angularVersion = ctx.getAngularVersion();
        if (assertNotTs(ctx) || !angularVersion.isPresent()) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }


    public static boolean refOnlyVisible(AnActionEvent anActionEvent) {
        if (assertNotRef(new IntellijFileContext(anActionEvent))) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return true;
        }
        return false;
    }

    public static boolean hasAngularVersion(AnActionEvent anActionEvent, AngularVersion angularVersion) {
        return AngularIndex.isAngularVersion(new IntellijFileContext(anActionEvent), angularVersion);

    }
}
