package actions_ng;

import actions.CreateTnDecProject;
import actions_all.shared.VisibleAssertions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Create an Angular 2 project
 */
public class CreateNgProject extends CreateTnDecProject {

    public static final String NG_PROJECT_LAYOUT = "/resources/projectLayout/ngPro";

    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.tnNoProject(anActionEvent);
    }

    @NotNull
    protected String getResourcePath() {
        return NG_PROJECT_LAYOUT;
    }

    @NotNull
    protected String getSubPath() {
        return "projectLayout/ngPro/";
    }

    @NotNull
    @Override
    protected String getTitle() {
        return "Create Angular NG Project";
    }

    protected boolean isAngular1() {
        return false;
    }
}
