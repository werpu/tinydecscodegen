package net.werpu.tools.supportive.transformations.modelHelpers;

import net.werpu.tools.supportive.fs.common.PsiElementContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.TYPE_SCRIPT_SINGLE_TYPE;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.TYPE_SCRIPT_VARIABLE;

public class ParameterDeclaration extends ExternalVariable {
    public ParameterDeclaration(@NotNull PsiElementContext variableDef) {
        super();

        this.variableDef = variableDef;
        //we now get everything possible
        externalizable = false;
        variableName = variableDef.getName();
        Optional<PsiElementContext> typeDcl = variableDef.$q(TYPE_SCRIPT_SINGLE_TYPE).findFirst();
        variableType = typeDcl.map(PsiElementContext::getText).orElse("any");
        variableUsages = new ArrayList<>();
    }
}
