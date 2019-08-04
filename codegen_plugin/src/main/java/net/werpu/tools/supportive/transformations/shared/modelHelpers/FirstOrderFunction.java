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

package net.werpu.tools.supportive.transformations.shared.modelHelpers;


import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.JS_DEFINITION_EXPRESSION;

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
     * this is the function body
     * aka {....}
     */
    PsiElementContext functionBody;


    /**
     * the original parameter list coming in
     * the way I see it we can treat them modelwise the same
     * as the external variables, but with a different attribute
     * as model holder
     */
    List<ParameterDeclaration> parameterList;

    /**
     * a list of variable parsing definition which possibly can
     * be externalized and dragged in
     * the variables need to be marked with a flag whether they are externalizable or not
     * a single false on this one and the externalization cannot  happen
     */
    List<ExternalVariable> exernalizables = newArrayList();


    @Setter
    String refactoredContent;


    public FirstOrderFunction(PsiElementContext functionElement, PsiElementContext functionHeader, PsiElementContext functionBody, List<ParameterDeclaration> parameterList, List<ExternalVariable> exernalizables) {
        this.functionElement = functionElement;
        this.functionHeader = functionHeader;
        this.functionBody = functionBody;
        this.parameterList = parameterList;
        this.exernalizables = exernalizables;
    }

    /**
     * final conclusion if a first order function
     * is externalizable from its shell method/function
     */
    public boolean isExternalizale() {
        //function needs to be this assignable
        return this.functionElement.getText().startsWith("this.") &&
                exernalizables.isEmpty();
    }

    /**
     * rewrite of the first order function as externalizable
     * @return
     */
    public String toExternalString() {
        if(!this.isExternalizale()) {
            return "";
        }
        if(!Strings.isNullOrEmpty(refactoredContent)) {
            return refactoredContent;
        }
        StringBuilder retVal = new StringBuilder();
        retVal.append(getFunctionName());
        retVal.append("(");
        retVal.append(getParametersAsStr());
        retVal.append(")");
        retVal.append(functionBody.getText());


        return retVal.toString();
    }

    public String getFunctionName() {
        String name = functionElement.$q(JS_DEFINITION_EXPRESSION).map(e -> e.getText()).findFirst().get();
        if(isNeedsToBeDeclared() && name.split("\\.").length == 2) {
            name = name.substring("this.".length());
        }
        return name;
    }

    public boolean isNeedsToBeDeclared() {
        String name = functionElement.$q(JS_DEFINITION_EXPRESSION).map(e -> e.getText()).findFirst().get();
        return StringUtils.isThis(name);
    }

    public String getDeclarationName() {
        String name = functionElement.$q(JS_DEFINITION_EXPRESSION).map(e -> e.getText()).findFirst().get();
        return name.substring(name.lastIndexOf(".")+1);

    }

    @NotNull
    public String getParametersAsStr() {
        return (parameterList == null || parameterList.isEmpty()) ? "" : parameterList.stream()
                .map(functionParameter -> functionParameter.getVariableName()+":"+functionParameter.getVariableType())
                .reduce((par1, par2) -> par1+","+par2).get();
    }

}
