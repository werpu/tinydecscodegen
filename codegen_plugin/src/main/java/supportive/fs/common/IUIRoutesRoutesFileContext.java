package supportive.fs.common;

import supportive.fs.Route;

/**
 * common interface for all route context classes
 */
public interface IUIRoutesRoutesFileContext {
    void addRoute(Route routeData);

    boolean isUrlInUse(Route routeData);

    boolean isRouteVarNameUsed(Route routeData);

    boolean isRouteNameUsed(Route routeData);
}
