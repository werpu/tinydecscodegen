package net.werpu.tools.supportive.transformations.modelHelpers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.supportive.fs.common.PsiElementContext;

import java.util.List;

@Getter
@AllArgsConstructor
public class GenericFunction {
    String functionName;

    List<ParameterDeclaration> parameterList;
    PsiElementContext functionBody;




    public String getParametersAsString() {
        return (parameterList == null || parameterList.isEmpty()) ? "" : parameterList.stream()
                .map(functionParameter -> functionParameter.getVariableName()+":"+functionParameter.getVariableType())
                .reduce((par1, par2) -> par1+","+par2).get();
    }

    public String toExternalString() {
        return functionName+"("+getParametersAsString()+")" + functionBody.getText();
    }
}
