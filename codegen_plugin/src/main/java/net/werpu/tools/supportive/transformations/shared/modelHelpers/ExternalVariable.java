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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.TYPE_SCRIPT_SINGLE_TYPE;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.TYPE_SCRIPT_VARIABLE;

/**
 * Parsing description of an externalizable variable
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExternalVariable implements Cloneable {

    /**
     * a boolean flag which marks the var
     */
    @Setter
    boolean externalizable;

    /**
     * the variable definition part,
     * which might not be changed but might be used
     * to get further info in its name and type
     *
     * aka var/let &lt;variableName&gt;[:&lt;type;&gt;] = ....
     */
    @NotNull
    PsiElementContext variableDef;

    @NotNull
    String variableName;

    /**
     * variable type if possible
     */
    @NotNull
    String variableType;

    /**
     * the psi elements defining the variable usages in its
     * respective usage context
     */
    @NotNull
    List<PsiElementContext> variableUsages;


    public ExternalVariable(@NotNull PsiElementContext variableDef) {
        this.variableDef = variableDef;
        //we now get everything possible
        externalizable = false;
        variableName = variableDef.$q(TYPE_SCRIPT_VARIABLE).findFirst().get().getName();
        Optional<PsiElementContext> typeDcl = variableDef.$q(TYPE_SCRIPT_SINGLE_TYPE).findFirst();
        variableType = typeDcl.map(PsiElementContext::getText).orElse("any");
        variableUsages = new ArrayList<>();
    }
}
