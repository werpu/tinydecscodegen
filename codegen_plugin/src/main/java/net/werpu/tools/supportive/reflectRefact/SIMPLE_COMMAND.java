package net.werpu.tools.supportive.reflectRefact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * enum to isolate parts
 * of the grammar for better code and
 * readability
 */
@Getter
@AllArgsConstructor
public enum SIMPLE_COMMAND {

    RE_TEXT_EQ("RE_TEXT_EQ", PsiWalkFunctions.RE_TEXT_EQ),
    CHILD_ELEM("CHILD_ELEM", PsiWalkFunctions.CHILD_ELEM),
    RE_TEXT_STARTS_WITH("RE_TEXT_STARTS_WITH", PsiWalkFunctions.RE_TEXT_STARTS_WITH),
    RE_NAME_EQ("RE_NAME_EQ", PsiWalkFunctions.RE_NAME_EQ),
    RE_NAME_STARTS_WITH("RE_NAME_STARTS_WITH", PsiWalkFunctions.RE_NAME_STARTS_WITH),
    RE_PARENTS_EQ("RE_PARENTS_EQ", PsiWalkFunctions.RE_PARENTS_EQ),
    P_FIRST("P_FIRST", PsiWalkFunctions.P_FIRST),
    P_PARENTS("P_PARENTS", PsiWalkFunctions.P_PARENTS),
    P_LAST("P_LAST", PsiWalkFunctions.P_LAST);

    private String name;
    private String value;

    /**
     *
     * resolve the current parsing token from a given value
     *
     * note we have a performance shortcut in a way that
     * we resolve eq and starts with with a simple regexp
     * that way we can avoid deep parsing
     * , but to resolve that we have to do a pre match to check
     * if the passed token string matches our patterns
     *
     * @param value the value which should match one way or the other
     * @return the enum token value
     */
    @Nullable
    public static SIMPLE_COMMAND fromValue(String value) {
        return Arrays.stream(SIMPLE_COMMAND.values())
                .filter(e -> {
                    if(e.getName().startsWith("RE_")) {
                        return value.matches(e.getValue());
                    } else {
                        return value.equals(e.getValue());
                    }
                })
                .findFirst().orElse(null);
    }

    /**
     * straight forward enum token from name
     * conversion
     * @param name the name
     * @return the enum token value
     */
    @Nullable
    public static SIMPLE_COMMAND fromName(String name) {
        return Arrays.stream(SIMPLE_COMMAND.values())
                .filter(e -> e.getName().equals(name))
                .findFirst().orElse(null);
    }

}