package dtos;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * json dto for NgModule decorator
 */
@Data
public class NgModuleJson {
    String name;
    String[] declarations;
    String[] imports;
    String[] exports;


    public void appendDeclare(String declareClass) {
        List<String> declares = Lists.newArrayList(Arrays.asList(this.declarations == null ? new String[0] : this.declarations));
        if(declares.contains(declareClass)) {
            return;
        }
        declares.add(declareClass);
        this.declarations = declares.toArray(new String[declares.size()]);
    }

    public void appendImport(String declareClass) {
        List<String> imports = Lists.newArrayList(Arrays.asList(this.imports == null ? new String[0] : this.imports));
        if(imports.contains(declareClass)) {
            return;
        }
        imports.add(declareClass);
        this.imports = imports.toArray(new String[imports.size()]);
    }

    public void appendExport(String declareClass) {
        List<String> exports = Lists.newArrayList(Arrays.asList(this.exports == null ? new String[0] : this.exports));
        if(exports.contains(declareClass)) {
            return;
        }
        exports.add(declareClass);
        this.exports = exports.toArray(new String[exports.size()]);
    }
}
