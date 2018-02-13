package supportive.fs.common;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;



public class Route implements Cloneable, Serializable, Comparable{

    @NonNull
    private String routeKey;

    @NonNull
    @Setter
    @Getter
    private String url;

    @NonNull
    @Setter
    @Getter
    private String component;

    @Setter
    @Getter
    private String routeVarName;

    @Getter
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


    public String getRouteKey() {
        return (Strings.isNullOrEmpty(routeKey)) ? urlToRouteKey(url) : routeKey;
    }

    private String urlToRouteKey(String url) {
        url = url.replaceAll("[\\/]*(.*)[\\/]*$", "$1");
        return url.replaceAll("\\/", ".");
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if(!(o instanceof Route)) {
            return -1;
        }
        int routeKey = Strings.nullToEmpty(((Route) o).getRouteKey()).compareTo(Strings.nullToEmpty(getRouteKey()));
        int url = Strings.nullToEmpty(((Route) o).getUrl()).compareTo(Strings.nullToEmpty(getUrl()));
        int component = Strings.nullToEmpty(((Route) o).getComponent()).compareTo(Strings.nullToEmpty(getComponent()));

        int weight = routeKey * 256 + url * 16 + component;
        return (weight > 0) ? -1:  (weight == 0) ? 0 : 1;
    }
}