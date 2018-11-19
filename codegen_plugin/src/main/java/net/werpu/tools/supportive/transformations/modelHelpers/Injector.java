package net.werpu.tools.supportive.transformations.modelHelpers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Injector {
    String name;
    String tsNameType;


    @Override
    public String toString() {
        return "@Inject('"+name+"') private "+tsNameType;
    }
}
