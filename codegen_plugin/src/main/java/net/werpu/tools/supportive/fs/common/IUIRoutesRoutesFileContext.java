package net.werpu.tools.supportive.fs.common;

import java.util.List;

/**
 * common interface for all route context classes
 */
public interface IUIRoutesRoutesFileContext {
    void addRoute(Route routeData);

    boolean isUrlInUse(Route routeData);

    boolean isRouteVarNameUsed(Route routeData);

    boolean isRouteNameUsed(Route routeData);


    List<PsiRouteContext> getRoutes();
}
