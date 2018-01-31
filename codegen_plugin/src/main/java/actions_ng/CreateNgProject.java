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

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
//       anActionEvent.getProject().getBaseDir().refresh(true, true);
//        RunManager  manager = RunManager.getInstance(anActionEvent.getProject());

//        Optional<ConfigurationType> ct = manager.getConfigurationFactoriesWithoutUnknown().parallelStream().filter(c -> c.getId().equals("js.build_tools.npm")).findFirst();

//        RunnerAndConfigurationSettings selectedConfiguration = manager.createRunConfiguration("Run Angular NG App", ct.get().getConfigurationFactories()[0]);

        //ProgramRunnerUtil.
        //RunCon

//        NpmRunConfiguration conf = selectedConfiguration.getConfiguration().
//        return;
        super.actionPerformed(anActionEvent);
    }

    protected void addRunConfig(Project project) {


            //RunnerAndConfigurationSettings selectedConfiguration = manager.createRunConfiguration("Run Angular NG App",);
            //TestNGConfiguration configuration = (TestNGConfiguration) selectedConfiguration.getConfiguration();
            //configuration.setVMParameters("-ea");
    }
}
