package supportive.fs;

import lombok.*;

@Getter
public class Route {

    @NonNull
    private String routeKey;

    @NonNull
    @Setter
    private String url;
    @NonNull
    private String component;

    @Setter
    private String routeVarName;

    String componentPath;

    public Route(String routeKey, String url, String component) {
        setRouteKey(routeKey);
        this.url = url;
        this.component = component;
    }

    public Route(String routeKey, String url, String component, String routeVarName, String componentPath) {
        this(routeKey, url, component);
        this.routeVarName = routeVarName;
        this.componentPath = componentPath;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
        this.routeVarName = routeKey.replaceAll("\\.", "_");
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

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath
                .replaceAll("\\\\", "/")
                .replaceAll("^(.*)\\.ts$", "$1");
    }

    public String getInclude() {
        return String.format("import {%s} from \"%s\";", component, componentPath);
    }
}
