package net.werpu.tools.supportive.transformations.modelHelpers;


import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.supportive.fs.common.PsiElementContext;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * defines the data needed to refactor
 * parts of a first order function
 */
@Getter
@AllArgsConstructor
public class FirstOrderFunction {

    /**
     * the entire function element
     *
     * aka var/let &lt;functionName&gt; = (&lt;arguments&gt;) => {..}
     * aka var/let &lt;functionName&gt; = function(&lt;arguments&gt;) {..}
     */
    PsiElementContext functionElement;

    /**
     * to avoid unneccesary parsing we parse the headers
     * and bodies upfront
     *
     * this is the function header
     * aka  (&lt;arguments&gt;
     * or function(&lt;arguments&gt;)
     */
    PsiElementContext functionHeader;

    /**
     * to avoid unneccesary parsing we parse the headers
     * and bodies upfront
     *
     * this is the function header
     * aka {....}
     */
    PsiElementContext functionBody;


    /**
     * the original parameter list coming in
     * the way I see it we can treat them modelwise the same
     * as the external variables, but with a different attribute
     * as model holder
     */
    List<ExternalVariable> parameterList;

    /**
     * a list of variable parsing definition which possibly can
     * be externalized and dragged in
     * the variables need to be marked with a flag whether they are externalizable or not
     * a single false on this one and the externalization cannot  happen
     */
    List<ExternalVariable> exernalizables = newArrayList();




    /**
     * final conclusion if a first order function
     * is externalizable from its shell method/function
     */
    public boolean isExternalizale() {
        //function needs to be this assignable
        return this.functionElement.getText().startsWith("this.") &&
                exernalizables.isEmpty();
    }

}
