package net.werpu.tools.factories;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import net.werpu.tools.gui.TTIcons;

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
    public static final String TPL_ANNOTATED_NG_PIPE = "Annotated NgPipe";
    public static final String TPL_ANNOTATED_NG_DIRECTIVE = "Annotated NgDirective";
    public static final String TPL_RUN_CONFIG = "RunConfig";
    public static final String TPL_SPRING_REST = "SpringRestController";
    public static final String TPL_SPRING_REST_METHOD = "SpringRestMethod";
    public static final String TPL_DTO = "Dto";
    public static final String TPL_ENUM = "TS String Enum";
    public static final String TPL_TN_REST_SERVICE = "Annotated Rest Service";
    public static final String TPL_TN_NG_REST_SERVICE = "NG Rest Service";
    public static final String TPL_TN_DEC_MODULE_TRANSFORMATION = "TnDecModuleTransformation";

    public static final String TPL_EXT = "ts";
    public static final String TPL_EXT_XML = "ts";
    public static final String TPL_EXT_JAVA = "java";

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("Tiny Decorations", TTIcons.LogoSm);

        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_COMPONENT + "." + TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_DIRECTIVE + "." + TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_CONTROLLER + "." + TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_SERVICE + "." + TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_MODULE + "." + TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_CONFIG + "." + TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_RUN + "." + TPL_EXT, TTIcons.LogoSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_NG_SERVICE + "." + TPL_EXT, TTIcons.LogoNgSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_NG_MODULE + "." + TPL_EXT, TTIcons.LogoNgSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_NG_COMPONENT + "." + TPL_EXT, TTIcons.LogoNgSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_NG_PIPE + "." + TPL_EXT, TTIcons.LogoNgSm));
        group.addTemplate(new FileTemplateDescriptor(TPL_ANNOTATED_NG_DIRECTIVE + "." + TPL_EXT, TTIcons.LogoNgSm));

        group.addTemplate(new FileTemplateDescriptor(TPL_DTO + "." + TPL_EXT));
        group.addTemplate(new FileTemplateDescriptor(TPL_ENUM + "." + TPL_EXT));
        group.addTemplate(new FileTemplateDescriptor(TPL_TN_REST_SERVICE + "." + TPL_EXT));
        group.addTemplate(new FileTemplateDescriptor(TPL_TN_NG_REST_SERVICE + "." + TPL_EXT));

        group.addTemplate(new FileTemplateDescriptor(TPL_RUN_CONFIG + "." + TPL_EXT_XML));
        group.addTemplate(new FileTemplateDescriptor(TPL_SPRING_REST + "." + TPL_EXT_JAVA));
        group.addTemplate(new FileTemplateDescriptor(TPL_SPRING_REST_METHOD + "." + TPL_EXT_JAVA));

        group.addTemplate(new FileTemplateDescriptor(TPL_TN_DEC_MODULE_TRANSFORMATION+"."+TPL_EXT));

        return group;
    }
}
