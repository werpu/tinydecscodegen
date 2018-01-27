package dtos;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * json dto for NgModule decorator
 */

public class NgModuleJson  extends BaseModuleJson{
    public NgModuleJson() {
        super();
    }

    public NgModuleJson(String name, String[] declarations, String[] imports, String[] exports, String[] providers) {
        super(name, declarations, imports, exports, providers);
    }
}
