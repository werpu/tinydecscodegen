package dtos;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ComponentAttribute {
    private String name = "";
    private ArgumentType argumentType = ArgumentType.Input;
    private boolean optional = false;
}
