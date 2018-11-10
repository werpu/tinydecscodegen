package supportive.reflectRefact;

import reflector.utils.ReflectUtils;
import supportive.dtos.ModuleElementScope;
import supportive.fs.common.IntellijFileContext;
import supportive.fs.common.NgModuleFileContext;

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
