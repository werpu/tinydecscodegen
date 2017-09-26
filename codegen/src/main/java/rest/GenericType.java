package rest;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class GenericType {

    @NonNull
    private final String ownerType;
    @NonNull
    private final List<GenericType> childTypes;


    public String toTypescript(Function<String, String>... reducers) {
        StringBuilder retVal = new StringBuilder();

        String finalOwnerType = ownerType;
        for (Function<String, String> reducer : reducers) {
            finalOwnerType = reducer.apply(finalOwnerType);
        }




        List<String> generics = childTypes.stream().map(inGeneric -> inGeneric.toTypescript(reducers)).collect(Collectors.toList());
        if (!generics.isEmpty()) {
            if (!Strings.isNullOrEmpty(finalOwnerType) && !finalOwnerType.startsWith("Map")) {
                retVal.append(finalOwnerType);
                retVal.append("<");
                retVal.append(Joiner.on(",").join(generics));
                retVal.append(">");
            } else if (!Strings.isNullOrEmpty(finalOwnerType) && finalOwnerType.startsWith("Map") && generics.size() > 1) {
                retVal.append("");
                retVal.append("").append("{[key:")
                        .append(generics.get(0)).append("]:")
                        .append(Joiner.on(",").join(generics.subList(1, generics.size())))
                        .append("}");
            } else if (!Strings.isNullOrEmpty(finalOwnerType) && finalOwnerType.startsWith("Map") && generics.size() < 1) {
                retVal.append("");
                retVal.append("{[")
                        .append(generics.get(0)).append("]:")
                        .append(Joiner.on(",").join(generics.subList(1, generics.size())))
                        .append("}")
                        .append("");
            } else {
                retVal.append(Joiner.on(",").join(generics));
            }

        } else {
            retVal.append(finalOwnerType);
        }
        return retVal.toString();
    }
}
