package net.werpu.tools.supportive.transformations.modelHelpers;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
