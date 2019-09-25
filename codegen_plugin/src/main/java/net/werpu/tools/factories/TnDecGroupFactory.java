/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

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
    public static final String TPL_I18N_JSON_FILE = "JSON I18N File";
    public static final String TPL_I18N_TS_FILE = "JSON I18N TS File";
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
    public static final String TPL_TN_DEC_COMPONENT_TRANSFORMATION = "TnDec Component Transformation";
    public static final String TPL_TN_DEC_DIRECTIVE_TRANSFORMATION = "TnDec Directive Transformation";
    public static final String TPL_NG_MODULE_TRANSFORMATION = "NgModuleTransformation";
    public static final String TPL_EXT = "ts";
    public static final String TPL_EXT_XML = "xml";
    public static final String TPL_EXT_JAVA = "java";
    public static final String TPL_EXT_JS = "js";

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

        group.addTemplate(new FileTemplateDescriptor(TPL_TN_DEC_MODULE_TRANSFORMATION + "." + TPL_EXT));
        group.addTemplate(new FileTemplateDescriptor(TPL_NG_MODULE_TRANSFORMATION + "." + TPL_EXT));
        group.addTemplate(new FileTemplateDescriptor(TPL_TN_DEC_COMPONENT_TRANSFORMATION + "." + TPL_EXT));
        group.addTemplate(new FileTemplateDescriptor(TPL_TN_DEC_DIRECTIVE_TRANSFORMATION + "." + TPL_EXT));
        group.addTemplate(new FileTemplateDescriptor(TPL_I18N_JSON_FILE + "." + TPL_EXT_JS));
        group.addTemplate(new FileTemplateDescriptor(TPL_I18N_TS_FILE + "." + TPL_EXT));

        return group;
    }
}
