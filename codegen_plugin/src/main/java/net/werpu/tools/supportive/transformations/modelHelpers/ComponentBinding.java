package net.werpu.tools.supportive.transformations.modelHelpers;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class ComponentBinding {
    @EqualsAndHashCode.Exclude
    BindingType bindingType;
    String name;

    public String toString() {
        StringBuilder retVal = new StringBuilder();

        switch (bindingType) {
            case BOTH: {
                retVal.append("@Both() ");
                retVal.append(name);
                retVal.append(": any;");
                break;
            }
            case INPUT: {
                retVal.append("@Input() ");
                retVal.append(name);
                retVal.append(": any;");
                break;
            }
            case ASTRING: {
                retVal.append("@AString() ");
                retVal.append(name);
                retVal.append(": string;");
                break;
            }
            case FUNC: {
                retVal.append("@Func() ");
                retVal.append(name);
                retVal.append(": Function;");
                break;
            }
            case OPT_BOTH: {
                retVal.append("@Both(true) ");
                retVal.append(name);
                retVal.append(": any;");
                break;
            }
            case OPT_INPUT: {
                retVal.append("@Input() ");
                retVal.append(name);
                retVal.append(": any;");
                break;
            }
            case OPT_FUNC: {
                retVal.append("@Func() ");
                retVal.append(name);
                retVal.append(": Function;");
                break;
            }
            case OPT_ASTRING: {
                retVal.append("@AString() ");
                retVal.append(name);
                retVal.append(": string;");
                break;
            }
            default:
                break;

        }
        return retVal.toString();
    }

}
