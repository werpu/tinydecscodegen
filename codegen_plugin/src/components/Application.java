package components;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.util.io.FileUtil;
import factories.TnDecGroupFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Application wide registration
 *
 * for now we just register our file templates
 * defined by teh group which itself
 * is an extension point in the xml (wtf??)
 */
public class Application implements ApplicationComponent {

    public static final String TEMPLATE_PATH = "/fileTemplates/Tiny Decorations/";

    public Application() {
    }



    @Override
    public void initComponent() {
        addFileTemplate(TnDecGroupFactory.TPL_ANNOTATED_COMPONENT, TnDecGroupFactory.TPL_EXT);
        addFileTemplate(TnDecGroupFactory.TPL_ANNOTATED_CONTROLLER, TnDecGroupFactory.TPL_EXT);
    }

    @Override
    public void disposeComponent() {

    }

    @Override
    @NotNull
    public String getComponentName() {
        return "Tiny Decorations";
    }

    //we need to register the template file content
    private static void addFileTemplate(final @NonNls String name, @NonNls
            String ext) {

        FileTemplate template = FileTemplateManager.getDefaultInstance().getTemplate(name);
        if (template == null) {
            try {
                template =
                        FileTemplateManager.getDefaultInstance().addTemplate(name, ext);
                template.setText(FileUtil.loadTextAndClose(
                        new
                                InputStreamReader(Application.class.getResourceAsStream(TEMPLATE_PATH
                                + name + "." + TnDecGroupFactory.TPL_EXT + ".ft")))
                );
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }
}
