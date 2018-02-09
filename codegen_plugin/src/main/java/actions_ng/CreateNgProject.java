package actions_ng;

import actions.CreateTnDecProject;
import org.jetbrains.annotations.NotNull;

/**
 * Create an Angular 2 project
 */
public class CreateNgProject extends CreateTnDecProject {

    public static final String NG_PROJECT_LAYOUT = "/resources/projectLayout/ngPro";

    @NotNull
    protected String getResourcePath() {
        return NG_PROJECT_LAYOUT;
    }

    @NotNull
    protected String getSubPath() {
        return "projectLayout/ngPro/";
    }


    protected boolean isAngular1() {
        return false;
    }
}
