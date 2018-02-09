package actions.shared;

import supportive.fs.common.IntellijFileContext;

public class VisibleAssertions {
    public static boolean assertNotJavaRest(IntellijFileContext ctx) {
        return ctx.getProject() == null ||
                !ctx.getPsiFile().getVirtualFile().getPath().endsWith(".java") ||
                (assertNotSprinRest(ctx) &&
                ctx.getDocument().getText().indexOf("@Path") != -1);
    }

    public static boolean assertNotSprinRest(IntellijFileContext ctx) {
        return ctx.getDocument().getText().indexOf("@RestController") == -1;
    }

    public static boolean assertNotJava(IntellijFileContext ctx) {
        return ctx.getPsiFile() == null ||
                !ctx.getPsiFile().getVirtualFile().getPath().endsWith(".java");
    }

    public static boolean assertNotTs(IntellijFileContext ctx) {
        return ctx.getPsiFile() == null || !ctx.getPsiFile().getVirtualFile().getPath().endsWith(".ts");
    }

    public static boolean assertTemplated(IntellijFileContext ctx) {
        return ctx.getDocument().getText().contains("@Component") ||
                ctx.getDocument().getText().contains("@Directive") ||
                ctx.getDocument().getText().contains("@Controller");
    }
}
