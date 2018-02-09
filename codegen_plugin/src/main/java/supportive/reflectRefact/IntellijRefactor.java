package supportive.reflectRefact;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import dtos.NgRootModuleJson;
import org.jetbrains.annotations.NotNull;
import refactor.TinyRefactoringUtils;
import reflector.utils.ReflectUtils;
import supportive.dtos.ModuleElementScope;
import supportive.fs.common.IntellijFileContext;
import supportive.refactor.DummyInsertPsiElement;
import supportive.refactor.IRefactorUnit;
import supportive.refactor.RefactorUnit;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static refactor.TinyRefactoringUtils.clearComments;
import static refactor.TinyRefactoringUtils.fishComments;

public class IntellijRefactor {

    public static final String NG_MODULE = "@NgModule";



    public static IRefactorUnit generateAppendAfterImport(PsiFile origFile, String newImport) {
        Pattern p = Pattern.compile("((import[^\\n\\;]+)\\s+[=from]+\\s+[^\\n\\;]+[\\n\\;])");
        Matcher m = p.matcher(origFile.getText());
        int end = 0;

        while (m.find())
        {
            end = m.end(1);
        }

        return new RefactorUnit(origFile, new DummyInsertPsiElement(end), newImport);
    }


    @NotNull
    public static IRefactorUnit refactorAddExport(String className, PsiFile module, PsiElement element) {
        String elementText = element.getText();
        String rawData = elementText.substring(elementText.indexOf("(") + 1, elementText.lastIndexOf(")"));
        String refactoredData = NG_MODULE + "(" + appendExport(rawData, className) + ")";

        return new RefactorUnit(module, element, refactoredData);
    }

    @NotNull
    public static IRefactorUnit refactorAddImport(String className, PsiFile module, PsiElement element) {
        String elementText = element.getText();
        String rawData = elementText.substring(elementText.indexOf("(") + 1, elementText.lastIndexOf(")"));
        String refactoredData = NG_MODULE + "(" + appendImport(rawData, className) + ")";

        return new RefactorUnit(module, element, refactoredData);
    }

    @NotNull
    public static IRefactorUnit refactorAddDeclarations(String className, PsiFile module, PsiElement element) {
        String elementText = element.getText();
        String rawData = elementText.substring(elementText.indexOf("(") + 1, elementText.lastIndexOf(")"));
        String refactoredData = NG_MODULE + "(" + appendDeclare(rawData, className) + ")";

        return new RefactorUnit(module, element, refactoredData);
    }


    @NotNull
    public static IRefactorUnit refactorAddProvides(String className, PsiFile module, PsiElement element) {
        String elementText = element.getText();
        String rawData = elementText.substring(elementText.indexOf("(") + 1, elementText.lastIndexOf(")"));
        String refactoredData = NG_MODULE + "(" + appendProvides(rawData, className) + ")";

        return new RefactorUnit(module, element, refactoredData);
    }


    public static IRefactorUnit generateImport(String className, PsiFile module, String relativePath, String fileName) {
        if(fileName.endsWith(".ts")) {
            fileName = fileName.substring(0, fileName.length() - 3);
        }
        String reducedClassName = ReflectUtils.reduceClassName(className);
        String importStatement = "\nimport {" + reducedClassName + "} from \"" + relativePath + "/" + fileName + "\";";
        if(module.getText().contains(importStatement)) { //skip if existing
            return new RefactorUnit(module, new DummyInsertPsiElement(0), "");
        }
        return generateAppendAfterImport(module, importStatement);
    }


    private static String remapToTs(String in) {
        in = in.replaceAll("\"([^\"\\\\])","$1");

        in = unescapeJsonStr(in);
        return in;
    }

    private static String unescapeJsonStr(String in) {
        return in.replaceAll("\\\\\"","\"").replaceAll("\"\\\\","\"");
    }


    public static String appendDeclare(String inStr, String declare) {
        String comments = fishComments(inStr);
        inStr = clearComments(inStr);
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgRootModuleJson moduleData = gson.fromJson(escapeJsonStr(json.toString()), NgRootModuleJson.class);
        moduleData.appendDeclare(declare);
        return comments+remapToTs(gson.toJson(moduleData.isRootModule() ? moduleData: moduleData.toModule()));
    }

    public static String appendProvides(String inStr, String declare) {
        String comments = fishComments(inStr);
        inStr = clearComments(inStr);
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgRootModuleJson moduleData = gson.fromJson(escapeJsonStr(json.toString()), NgRootModuleJson.class);
        moduleData.appendProvides(declare);
        return comments+remapToTs(gson.toJson(moduleData.isRootModule() ? moduleData: moduleData.toModule()));

    }

    public static String appendImport(String inStr, String declare) {
        String comments = fishComments(inStr);
        inStr = clearComments(inStr);
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgRootModuleJson moduleData = gson.fromJson(escapeJsonStr(json.toString()), NgRootModuleJson.class);
        moduleData.appendImport(declare);
        return comments+remapToTs(gson.toJson(moduleData.isRootModule() ? moduleData: moduleData.toModule()));
    }

    public static String appendExport(String inStr, String declare) {
        String comments = fishComments(inStr);
        inStr = clearComments(inStr);
        inStr = inStr.replaceAll("\"","\\\"");
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        String jsonStr = json.toString();
        NgRootModuleJson moduleData = gson.fromJson(escapeJsonStr(jsonStr), NgRootModuleJson.class);
        moduleData.appendExport(declare);
        return comments+remapToTs(gson.toJson(moduleData.isRootModule() ? moduleData: moduleData.toModule()));
    }

    private static String escapeJsonStr(String jsonStr) {
        return jsonStr.replaceAll("\"([^\"]+)\"","\"\\\\\"$1\\\\\"\"");
    }


    public static void appendDeclarationToModule(IntellijFileContext fileContext, ModuleElementScope scope, String className, String fileName) throws IOException {


        List<IntellijFileContext> annotatedModules = fileContext.findFirstUpwards(psiFile -> psiFile.getContainingFile().getText().contains(NG_MODULE));

        for (IntellijFileContext angularModule : annotatedModules) {
            String relativePath = fileContext.getVirtualFile().getPath().replaceAll(angularModule.getParent().get().getVirtualFile().getPath(), ".");

            List<IRefactorUnit> refactoringsToProcess = angularModule
                    .findPsiElements(PsiWalkFunctions::isNgModule)
                    .stream().map(refactorModuleDeclarations(angularModule, scope, className)).collect(Collectors.toList());

            List<IRefactorUnit> finalRefactorings = Lists.newArrayList();


            finalRefactorings.add(angularModule.refactorIn(psiFile -> IntellijRefactor.generateImport(className, psiFile, relativePath, fileName)));
            finalRefactorings.addAll(refactoringsToProcess);

            angularModule.refactorContent(finalRefactorings);
            angularModule.commit();

            angularModule
                    .findPsiElements(PsiWalkFunctions::isNgModule)
                    .forEach(psiElement -> CodeStyleManager.getInstance(fileContext.getProject()).reformat(psiElement));

        }
    }

    @NotNull
    public static Function<PsiElement, IRefactorUnit> refactorModuleDeclarations(IntellijFileContext angularModule, ModuleElementScope scope, String className) {
        return element -> {
            switch (scope) {
                case EXPORT:
                    return refactorAddExport(ReflectUtils.reduceClassName(className) , angularModule.getPsiFile(), element);
                case DECLARATIONS:
                    return refactorAddDeclarations(ReflectUtils.reduceClassName(className), angularModule.getPsiFile(), element);
                case IMPORT:
                    return refactorAddImport(ReflectUtils.reduceClassName(className), angularModule.getPsiFile(), element);

                default://provides
                    return refactorAddProvides(ReflectUtils.reduceClassName(className), angularModule.getPsiFile(), element);
            }
        };
    }
}
