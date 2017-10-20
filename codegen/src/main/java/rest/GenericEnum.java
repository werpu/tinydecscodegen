package rest;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.List;


@EqualsAndHashCode
@Getter
public class GenericEnum extends GenericClass {

    List<String> attributes;

    public GenericEnum(GenericType clazz, GenericEnum parentEnum, List<String> attributes) {
        super(clazz, parentEnum, Collections.emptyList(), Collections.emptyList());
        this.attributes = attributes;
    }


}
