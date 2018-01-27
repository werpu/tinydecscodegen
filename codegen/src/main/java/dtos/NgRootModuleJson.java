package dtos;

public class NgRootModuleJson extends BaseModuleJson {

    String[] bootstrap;

    public NgRootModuleJson() {
    }

    public NgRootModuleJson(String[] declarations, String[] imports, String[] exports, String[] providers, String[] bootstrap) {
        this.declarations = declarations;
        this.imports = imports;
        this.exports = exports;
        this.providers = providers;
        this.bootstrap = bootstrap;
    }

    public boolean isRootModule() {
        return bootstrap != null && bootstrap.length > 0;
    }

    public NgModuleJson toModule() {
        return new NgModuleJson(name, declarations, imports, exports, providers);
    }
}
