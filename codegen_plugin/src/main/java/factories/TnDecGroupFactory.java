package factories;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import gui.TTIcons;

public class TnDecGroupFactory implements FileTemplateGroupDescriptorFactory {

    public static final String TPL_ANNOTATED_COMPONENT = "Annotated Component";
    public static final String TPL_ANNOTATED_DIRECTIVE = "Annotated Directive";
    public static final String TPL_ANNOTATED_CONTROLLER = "Annotated Controller";
    public static final String TPL_ANNOTATED_SERVICE = "Annotated Service";
    public static final String TPL_ANNOTATED_NG_SERVICE = "Annotated NgService";
    public static final String TPL_ANNOTATED_FILTER = "Annotated Filter";
    public static final String TPL_ANNOTATED_MODULE = "Annotated Module";
    public static final String TPL_ANNOTATED_CONFIG = "Annotated Config";
    public static final String TPL_ANNOTATED_RUN = "Annotated Run";
    public static final String TPL_ANNOTATED_NG_MODULE = "Annotated NgModule";
    public static final String TPL_ANNOTATED_NG_COMPONENT = "Annotated NgComponent";

    public static final String TPL_EXT = "ts";

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("Tiny Decorations", TTIcons.LogoSm);

        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_COMPONENT+ "."+ TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_DIRECTIVE+ "."+ TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_CONTROLLER+ "."+ TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_SERVICE+ "."+ TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_MODULE+ "."+ TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_CONFIG+ "."+ TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_RUN+ "."+ TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_NG_SERVICE+ "."+ TPL_EXT));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_NG_MODULE+ "."+ TPL_EXT));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_NG_COMPONENT + "."+ TPL_EXT));

        return group;
    }
}
