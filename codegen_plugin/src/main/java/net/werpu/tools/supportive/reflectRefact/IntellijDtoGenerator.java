package net.werpu.tools.supportive.reflectRefact;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.project.Project;
import net.werpu.tools.factories.TnDecGroupFactory;
import rest.GenericClass;
import rest.GenericEnum;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntellijDtoGenerator  {

    public static String generate(Project project, List<GenericClass> classes) {
        final FileTemplate vslTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_DTO);
        final FileTemplate enumTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ENUM);

        return classes.stream().map((GenericClass item) -> {
            try {

                Map<String, Object> root = new HashMap<>();
                root.put("clazz", item);
                return FileTemplateUtil.mergeTemplate(root,(item instanceof GenericEnum) ? enumTemplate.getText() : vslTemplate.getText(), false);

            } catch (IOException e) {
                return e.getMessage();
            }
        }).reduce("", (s0, s1) -> s0 + "\n" + s1);
    }
}