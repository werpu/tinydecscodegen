package actions_ng;

import actions.CreateTnDecProject;
import org.jetbrains.annotations.NotNull;

public class CreateNgProject extends CreateTnDecProject {

    @NotNull
    protected String getResourcePath() {
        return "/resources/projectLayout/ngPro";
    }
}
