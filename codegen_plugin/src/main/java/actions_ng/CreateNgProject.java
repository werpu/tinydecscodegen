package actions_ng;

import actions.CreateTnDecProject;
import org.jetbrains.annotations.NotNull;

/**
 * Create an Angular 2 project
 */
public class CreateNgProject extends CreateTnDecProject {

    public static final String PROJECT_LAYOUT = "/resources/projectLayout/ngPro";

    @NotNull
    protected String getResourcePath() {
        return PROJECT_LAYOUT;
    }
}
