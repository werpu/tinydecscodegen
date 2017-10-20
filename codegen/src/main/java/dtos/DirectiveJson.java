package dtos;

import lombok.Getter;

import java.util.List;


public class DirectiveJson extends ComponentJson {

    @Getter
    private final String types;


    public DirectiveJson(String selector, String template, String controllerAs, String types, boolean transclude, List<String> transclusionSlots) {
        super(selector, template, controllerAs, transclude, transclusionSlots);
        this.types = types;
    }

}
