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

package dtos;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class BaseModuleJson {
    String name;
    String[] declarations;
    String[] imports;
    String[] exports;
    String[] providers;


    public BaseModuleJson() {
    }

    public BaseModuleJson(String name, String[] declarations, String[] imports, String[] exports, String[] providers) {
        this.name = name;
        this.declarations = declarations;
        this.imports = imports;
        this.exports = exports;
        this.providers = providers;
    }

    public void appendDeclare(String declareClass) {
        List<String> declares = Lists.newArrayList(Arrays.asList(this.declarations == null ? new String[0] : this.declarations));
        if(declares.contains(declareClass)) {
            return;
        }
        declares.add(declareClass);
        this.declarations = declares.toArray(new String[declares.size()]);
    }

    public void appendProvides(String declareClass) {
        List<String> providers = Lists.newArrayList(Arrays.asList(this.providers == null ? new String[0] : this.providers));
        if(providers.contains(declareClass)) {
            return;
        }
        providers.add(declareClass);
        this.providers = providers.toArray(new String[providers.size()]);
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
