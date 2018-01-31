package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import org.fest.util.Maps;
import utils.IntellijFileContext;
import utils.IntellijUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class CreateSpringRestController extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = IntellijUtils.getProject(event);
        VirtualFile folder = IntellijUtils.getFolderOrFile(event);
        IntellijFileContext ctx = new IntellijFileContext(project, folder);


        VirtualFile srcRoot = ProjectFileIndex.getInstance(project).getSourceRootForFile(folder);
        if(srcRoot != null) {//we are in a source root
            Path targetPath = Paths.get(ctx.getFolderPath());
            Path srcRootPath = Paths.get(srcRoot.getPath());

            String packageName = srcRootPath.relativize(targetPath).toString()
                    .replaceAll("[/\\\\]+", ".")
                    .replaceAll("^\\.(.*)\\.$", "$1" );

            Map<String, String> attrs = Maps.newHashMap();
            attrs.put("PACKAGE_NAME", packageName);



        }
        //dtermine the package

        //event.getProject().getWorkspaceFile().is

    }
}
