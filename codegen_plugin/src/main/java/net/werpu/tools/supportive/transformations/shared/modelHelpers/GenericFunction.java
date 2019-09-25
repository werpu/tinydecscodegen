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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.werpu.tools.supportive.fs.common.PsiElementContext;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class GenericFunction {
    private final String functionName;
    private final List<ParameterDeclaration> parameterList;
    private final PsiElementContext functionBody;
    @Setter
    String refactoredContent;

    public String getParametersAsString() {
        return (parameterList == null || parameterList.isEmpty()) ? "" : parameterList.stream()
                .map(functionParameter -> functionParameter.getVariableName() + ":" + functionParameter.getVariableType())
                .reduce((par1, par2) -> par1 + "," + par2).get();
    }

    public String toExternalString() {
        return (!Strings.isNullOrEmpty(refactoredContent)) ? refactoredContent : functionName + "(" + getParametersAsString() + ")" + functionBody.getText();
    }
}
