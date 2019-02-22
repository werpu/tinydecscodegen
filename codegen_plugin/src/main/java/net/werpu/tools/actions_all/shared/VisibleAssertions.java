package net.werpu.tools.actions_all.shared;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import net.werpu.tools.indexes.AngularIndex;
import net.werpu.tools.supportive.fs.common.AngularVersion;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.utils.IntellijUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.PSI_METHOD;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.ALL;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.TEXT_EQ;
import static net.werpu.tools.supportive.utils.IntellijUtils.getHtmlExtension;
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

    public static boolean assertNotHtml(IntellijFileContext ctx) {
        return ctx.getPsiFile() == null || !ctx.getPsiFile().getVirtualFile().getPath().endsWith(getHtmlExtension());
    }

    public static boolean assertTemplated(IntellijFileContext ctx) {
        return ctx.getText().contains("@Component") ||
                ctx.getText().contains("@Directive") ||
                ctx.getText().contains("@Controller");
    }

    public static boolean assertController(IntellijFileContext ctx) {
        return ctx.queryContent(JS_ES_6_DECORATOR, PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("Controller")).findFirst().isPresent() ||
                ctx.queryContent(JS_ES_6_DECORATOR, PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("Component")).findFirst().isPresent();

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


    public static void templateVisible(AnActionEvent anActionEvent) {
        if (IntellijUtils.getFolderOrFile(anActionEvent) == null) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        anActionEvent.getPresentation().setEnabledAndVisible(!(assertNotTs(ctx) && assertNotHtml(ctx)));



    }

    /**
     * Check for the current cursor being in a string
     * @param anActionEvent
     */
    public static void cursorInTemplate(AnActionEvent anActionEvent) {
        //if one of its parents is a string template

        Editor editor = IntellijUtils.getEditor(anActionEvent);
        final int cursorPos = editor.getCaretModel().getOffset();
        IntellijFileContext editorFile = new IntellijFileContext(anActionEvent);

        Predicate<PsiElementContext> positionFilter = el -> {
            int offSet = el.getTextRangeOffset();
            int offSetEnd = offSet + el.getText().length();
            return cursorPos >= offSet && cursorPos <= offSetEnd;
        };
        Optional<PsiElementContext> foundElement = editorFile.$q(ALL(SUB_QUERY(STRING_TEMPLATE_EXPR), SUB_QUERY(PSI_ELEMENT_JS_STRING_LITERAL))).filter(positionFilter).findFirst();
        if(!foundElement.isPresent()) {
            foundElement = editorFile.$q(SUB_QUERY(PSI_ELEMENT_JS_STRING_TEMPLATE_PART)).filter(positionFilter).findFirst();
        }

        anActionEvent.getPresentation().setEnabledAndVisible(foundElement.isPresent());
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
