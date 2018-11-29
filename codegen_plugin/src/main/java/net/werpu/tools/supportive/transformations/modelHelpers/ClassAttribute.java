package net.werpu.tools.supportive.transformations.modelHelpers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.supportive.fs.common.PsiElementContext;

@AllArgsConstructor
@Getter
public class ClassAttribute {


    PsiElementContext element;

    String name;
    String type;



    public ClassAttribute(PsiElementContext element) {
        this.element = element;
        name = element.getName();
        type = "any";
    }

    public String getText() {
        return getName()+": "+getType();
    }
}
