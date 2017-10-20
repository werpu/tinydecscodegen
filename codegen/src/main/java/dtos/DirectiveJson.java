package dtos;

import lombok.Getter;

@Getter
public class DirectiveJson extends ComponentJson {


    private final String types;


    public DirectiveJson(String selector, String template, String controllerAs, String types) {
        super(selector, template, controllerAs);
        this.types = types;
    }

}
