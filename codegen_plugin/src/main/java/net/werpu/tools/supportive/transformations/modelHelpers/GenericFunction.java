package net.werpu.tools.supportive.transformations.modelHelpers;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
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
                .map(functionParameter -> functionParameter.getVariableName()+":"+functionParameter.getVariableType())
                .reduce((par1, par2) -> par1+","+par2).get();
    }

    public String toExternalString() {
        return (!Strings.isNullOrEmpty(refactoredContent))?  refactoredContent : functionName+"("+getParametersAsString()+")" + functionBody.getText();
    }
}
