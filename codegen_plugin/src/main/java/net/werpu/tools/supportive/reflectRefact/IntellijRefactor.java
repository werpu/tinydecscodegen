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

package net.werpu.tools.supportive.reflectRefact;

import net.werpu.tools.supportive.dtos.ModuleElementScope;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.NgModuleFileContext;
import reflector.utils.ReflectUtils;

import java.io.IOException;
import java.util.List;

public class IntellijRefactor {
    public static final String NG_MODULE = "@NgModule";

    public static void appendDeclarationToModule(IntellijFileContext fileContext, ModuleElementScope scope, String className, String fileName) throws IOException {

        List<IntellijFileContext> annotatedModules = fileContext.findFirstUpwards(psiFile -> psiFile.getContainingFile().getText()
                .contains(NG_MODULE) && !psiFile.getVirtualFile().getPath()
                .replaceAll("\\\\", "/").equals(fileContext.getVirtualFile().getPath()));

        for (IntellijFileContext angularModule : annotatedModules) {
            NgModuleFileContext moduleFileContext = new NgModuleFileContext(angularModule);

            String relativePath = (fileContext.calculateRelPathTo(moduleFileContext))
                    .replaceAll("\\.ts$", "")
                    .replaceAll("\\/\\/", "/");
            String finalImportName = moduleFileContext.appendImport(ReflectUtils.reduceClassName(className), relativePath);

            switch (scope) {
                case EXPORT:
                    moduleFileContext.appendExports(finalImportName);
                    break;
                case DECLARATIONS:
                    moduleFileContext.appendDeclaration(finalImportName);
                    break;
                case IMPORT:
                    moduleFileContext.appendImports(finalImportName);
                    break;
                default://provides
                    moduleFileContext.appendProviders(finalImportName);
                    break;
            }
        }
    }
}
