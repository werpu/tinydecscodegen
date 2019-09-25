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

package net.werpu.tools.actions_all.shared;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import net.werpu.tools.indexes.AngularIndex;
import net.werpu.tools.supportive.fs.common.AngularVersion;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.utils.IntellijUtils;

import java.util.Optional;
import java.util.function.Predicate;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.ALL;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.TEXT_EQ;
import static net.werpu.tools.supportive.utils.IntellijUtils.getJsonExtension;
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

    public static boolean assertNotJson(IntellijFileContext ctx) {
        return ctx.getPsiFile() == null || !ctx.getPsiFile().getVirtualFile().getPath().endsWith(getJsonExtension());
    }

    public static boolean assertTs(IntellijFileContext ctx) {
        return !assertNotTs(ctx);
    }

    public static boolean assertJson(IntellijFileContext ctx) {
        return !assertNotJson(ctx);
    }

    public static boolean assertNotHtml(IntellijFileContext ctx) {
        return ctx.getPsiFile() == null || !ctx.getPsiFile().getVirtualFile().getFileType().getDefaultExtension().toLowerCase().equals("html");
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

    public static void anyAngularVisible(AnActionEvent anActionEvent) {
        if (IntellijUtils.getFolderOrFile(anActionEvent) == null) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        if (!ctx.isAngularChild(AngularVersion.TN_DEC) && !ctx.isAngularChild(AngularVersion.NG)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
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
     *
     * @param anActionEvent
     */
    public static void cursorInTemplate(AnActionEvent anActionEvent) {
        //if one of its parents is a string template

        if (!assertNotHtml(new IntellijFileContext(anActionEvent))) {
            anActionEvent.getPresentation().setEnabledAndVisible(true);
            return;
        }
        Editor editor = IntellijUtils.getEditor(anActionEvent);
        final int cursorPos = editor.getCaretModel().getOffset();
        IntellijFileContext editorFile = new IntellijFileContext(anActionEvent);

        Predicate<PsiElementContext> positionFilter = el -> {
            int offSet = el.getTextRangeOffset();
            int offSetEnd = offSet + el.getText().length();
            return cursorPos >= offSet && cursorPos <= offSetEnd;
        };
        Optional<PsiElementContext> foundElement = editorFile.$q(ALL(SUB_QUERY(STRING_TEMPLATE_EXPR), SUB_QUERY(PSI_ELEMENT_JS_STRING_LITERAL))).filter(positionFilter).findFirst();
        if (!foundElement.isPresent()) {
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
