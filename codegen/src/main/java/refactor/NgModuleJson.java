package refactor;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Setter
@Getter
public class NgModuleJson {
    String name;
    String[] declarations;
    String[] imports;
    String[] exports;


    public void appendDeclare(String declareClass) {
        List<String> declares = Lists.newArrayList(Arrays.asList(this.declarations == null ? new String[0] : this.declarations));
        declares.add(declareClass);
        this.declarations = declares.toArray(new String[declares.size()]);
    }

    public void appendImport(String declareClass) {
        List<String> declares = Lists.newArrayList(Arrays.asList(this.imports == null ? new String[0] : this.imports));
        declares.add(declareClass);
        this.imports = declares.toArray(new String[declares.size()]);
    }

    public void appendExport(String declareClass) {
        List<String> exports = Lists.newArrayList(Arrays.asList(this.exports == null ? new String[0] : this.exports));
        exports.add(declareClass);
        this.exports = exports.toArray(new String[exports.size()]);
    }
}
