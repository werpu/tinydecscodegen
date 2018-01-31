package actions_ng;

import actions.CreateTnDecProject;
import com.intellij.ConfigurableFactory;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
