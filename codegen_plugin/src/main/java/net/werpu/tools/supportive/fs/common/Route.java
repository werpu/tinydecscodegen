/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.supportive.fs.common;

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import static net.werpu.tools.supportive.utils.StringUtils.normalizePath;


@Getter
@Setter
@EqualsAndHashCode
public class Route implements Cloneable, Serializable, Comparable {

    @NonNull
    private String routeKey;

    private String viewName;

    private String url;

    private String component;

    private String routeVarName;

    String componentPath;


    Class originContext;

    public Route(String routeKey, String url, String component, Class origin) {
        setRouteKey(routeKey);
        this.url = url;
        this.component = component;
        this.originContext = origin;
    }

    public Route(String routeKey, String url, String component, String routeVarName, String componentPath, Class origin) {
        this(routeKey, url, component, origin);
        this.routeVarName = routeVarName;
        this.componentPath = componentPath;
    }

    public Route(String viewName, String routeKey, String url, String component, String routeVarName, String componentPath, Class origin) {
        this(routeKey, url, component, origin);
        this.routeVarName = routeVarName;
        this.componentPath = componentPath;
        this.viewName = viewName;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
        this.routeVarName = routeKey.replaceAll("\\.", "_");
    }


    public String toStringTnNg1() {
        String routeTemplateStr = "\n\n$stateProvider.state('%s',\n" +
                "    MetaData.routeData(%s,\n" +
                "        {\n" +
                "            url: '%s'\n" +
                "        }\n" +
                "    )\n" +
                ");\n";
        return String.format(routeTemplateStr, routeKey, component, url, component);
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

    public String toStringNg2UIRoutes() {

        if (component != null) {
            String routeTemplatesSimple = "export let %s: Ng2StateDeclaration = {name: '%s', url: '%s', component: %s }; \n";
            return String.format(routeTemplatesSimple, getRouteVarName(), routeKey, url, component);
        } else {
            String routeTemplatesSimple = "export let %s: Ng2StateDeclaration = {name: '%s', url: '%s' }; \n";
            return String.format(routeTemplatesSimple, getRouteVarName(), routeKey, url);
        }

    }

    public String toLocalRoutes() {
        String routeTemplatesSimple = "export let %s: ModuleWithProviders = UIRouterModule.forChild({states: [%s]}); \n";
        return String.format(routeTemplatesSimple, getLocalRouteDeclName(), getRouteVarName());
    }


    public void setComponentPath(String componentPath) {
        this.componentPath = normalizePath(componentPath)
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

    public String getLocalRouteDeclName() {
        return "lRte_"+getRouteVarName();
    }

    public String getRawComponentName() {
        return getComponent().replaceAll("\\s*\\<.*", "");
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (!(o instanceof Route)) {
            return -1;
        }
        int routeKey = Strings.nullToEmpty(((Route) o).getRouteKey()).compareTo(Strings.nullToEmpty(getRouteKey()));
        int url = Strings.nullToEmpty(((Route) o).getUrl()).compareTo(Strings.nullToEmpty(getUrl()));
        int component = Strings.nullToEmpty(((Route) o).getComponent()).compareTo(Strings.nullToEmpty(getComponent()));

        int weight = routeKey * 256 + url * 16 + component;
        return (weight > 0) ? -1 : (weight == 0) ? 0 : 1;
    }

    public Route clone() throws CloneNotSupportedException {
        return (Route) super.clone();
    }
}
