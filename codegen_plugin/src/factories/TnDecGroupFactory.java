package factories;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import gui.TTIcons;

public class TnDecGroupFactory implements FileTemplateGroupDescriptorFactory {

    public static final String TPL_ANNOTATED_COMPONENT = "Annotated Component";
    public static final String TPL_ANNOTATED_CONTROLLER = "Annotated Controller";
    public static final String TPL_ANNOTATED_SERVICE = "Annotated Service";

    public static final String TPL_EXT = "ts";

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("Tiny Decorations", TTIcons.LogoSm);

        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_COMPONENT+ "."+ TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_CONTROLLER+ "."+ TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_SERVICE+ "."+ TPL_EXT, TTIcons.LogoSm));

        return group;
    }
}
