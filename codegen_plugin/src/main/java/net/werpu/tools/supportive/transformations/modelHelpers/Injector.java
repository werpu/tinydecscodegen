package net.werpu.tools.supportive.transformations.modelHelpers;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Injector {
    String name;
    @EqualsAndHashCode.Exclude
    String tsNameType;


    @Override
    public String toString() {
        return "@Inject('"+name+"') private "+tsNameType;
    }


}
