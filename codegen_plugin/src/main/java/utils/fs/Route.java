package utils.fs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class Route {

    @NonNull
    private String routeKey;
    @NonNull
    private String url;
    @NonNull
    private String component;

    String componentInclude;


    public String getRouteVarName() {
        return routeKey.replaceAll("\\.", "_");
    }

    public String toStringNg1() {
        return "//TODO yet to be implemented \n";
    }

    public String toStringNg2() {

        if (component != null) {
            String routeTemplatesSimple = "let %s = {name: '%s', url: '%s', component: %s }; \n";
            return String.format(routeTemplatesSimple, getRouteVarName(), routeKey, url, component);
        } else {
            String routeTemplatesSimple = "let %s = {name: '%s', url: '%s' }; \n";
            return String.format(routeTemplatesSimple, getRouteVarName(), routeKey, url);
        }

    }
}
