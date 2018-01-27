package utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.codeStyle.CodeStyleManager;
import dtos.NgModuleJson;
import org.jetbrains.annotations.NotNull;
import refactor.TinyRefactoringUtils;
import reflector.utils.ReflectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IntellijRefactor {

    public static final String NG_MODULE = "@NgModule";


    /*public static void findTemplate(PsiFile psiJSFile) {
        final AtomicBoolean hasFound = new AtomicBoolean(false);
        PsiRecursiveElementWalkingVisitor myElementVisitor = new PsiRecursiveElementWalkingVisitor() {

            public void visitElement(PsiElement element) {
                if(hasFound.get()) {
                    return;
                }

                if (isTemplate(element)) {
                    hasFound.set(true);
                    stopWalking();
                    return;
                }
                super.visitElement(element);
            }
        };

        myElementVisitor.visitFile(psiJSFile);
    }*/






    public static RefactorUnit generateAppendAfterImport(PsiFile origFile, String newImport) {
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
    public static RefactorUnit refactorAddExport(String className, PsiFile module, PsiElement element) {
        String elementText = element.getText();
        String rawData = elementText.substring(elementText.indexOf("(") + 1, elementText.lastIndexOf(")"));
        String refactoredData = NG_MODULE + "(" + appendExport(rawData, className) + ")";

        return new RefactorUnit(module, element, refactoredData);
    }

    @NotNull
    public static RefactorUnit refactorAddImport(String className, PsiFile module, PsiElement element) {
        String elementText = element.getText();
        String rawData = elementText.substring(elementText.indexOf("(") + 1, elementText.lastIndexOf(")"));
        String refactoredData = NG_MODULE + "(" + appendImport(rawData, className) + ")";

        return new RefactorUnit(module, element, refactoredData);
    }

    @NotNull
    public static  RefactorUnit refactorAddDeclarations(String className, PsiFile module, PsiElement element) {
        String elementText = element.getText();
        String rawData = elementText.substring(elementText.indexOf("(") + 1, elementText.lastIndexOf(")"));
        String refactoredData = NG_MODULE + "(" + appendDeclare(rawData, className) + ")";

        return new RefactorUnit(module, element, refactoredData);
    }


    @NotNull
    public static  RefactorUnit refactorAddProvides(String className, PsiFile module, PsiElement element) {
        String elementText = element.getText();
        String rawData = elementText.substring(elementText.indexOf("(") + 1, elementText.lastIndexOf(")"));
        String refactoredData = NG_MODULE + "(" + appendProvides(rawData, className) + ")";

        return new RefactorUnit(module, element, refactoredData);
    }


    public static RefactorUnit generateImport(String className, PsiFile module, String relativePath) {
        String reducedClassName = ReflectUtils.reduceClassName(className);
        String importStatement = "\nimport {" + reducedClassName + "} from \"" + relativePath + "/" + reducedClassName + "\";";
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
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgModuleJson moduleData = gson.fromJson(escapeJsonStr(json.toString()), NgModuleJson.class);
        moduleData.appendDeclare(declare);
        return remapToTs(gson.toJson(moduleData));
    }

    public static String appendProvides(String inStr, String declare) {
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgModuleJson moduleData = gson.fromJson(escapeJsonStr(json.toString()), NgModuleJson.class);
        moduleData.appendDeclare(declare);
        return remapToTs(gson.toJson(moduleData));
    }

    public static String appendImport(String inStr, String declare) {
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgModuleJson moduleData = gson.fromJson(escapeJsonStr(json.toString()), NgModuleJson.class);
        moduleData.appendImport(declare);
        return remapToTs(gson.toJson(moduleData));
    }

    public static String appendExport(String inStr, String declare) {
        inStr = inStr.replaceAll("\"","\\\"");
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        String jsonStr = json.toString();
        NgModuleJson moduleData = gson.fromJson(escapeJsonStr(jsonStr), NgModuleJson.class);
        moduleData.appendExport(declare);
        return remapToTs(gson.toJson(moduleData));
    }

    private static String escapeJsonStr(String jsonStr) {
        return jsonStr.replaceAll("\"([^\"]+)\"","\"\\\\\"$1\\\\\"\"");
    }


    public static void appendDeclarationToModule(IntellijFileContext fileContext, ModuleElementScope scope, String className) throws IOException {


        List<IntellijFileContext> annotatedModules = fileContext.findFirstUpwards(psiFile -> psiFile.getContainingFile().getText().contains(NG_MODULE));

        for (IntellijFileContext angularModule : annotatedModules) {
            String relativePath = fileContext.getVirtualFile().getPath().replaceAll(angularModule.getParent().get().getVirtualFile().getPath(), ".");

            List<RefactorUnit> refactoringsToProcess = angularModule
                    .findPsiElements(PsiWalkFunctions::isNgModule)
                    .stream().map(refactorModuleDeclarations(angularModule, scope, className)).collect(Collectors.toList());

            List<RefactorUnit> finalRefactorings = Lists.newArrayList();


            finalRefactorings.add(angularModule.refactorIn(psiFile -> IntellijRefactor.generateImport(className, psiFile, relativePath)));
            finalRefactorings.addAll(refactoringsToProcess);

            angularModule.refactorContent(finalRefactorings);
            angularModule.commit();

            angularModule
                    .findPsiElements(PsiWalkFunctions::isNgModule)
                    .forEach(psiElement -> CodeStyleManager.getInstance(fileContext.getProject()).reformat(psiElement));

        }
    }

    @NotNull
    public static Function<PsiElement, RefactorUnit> refactorModuleDeclarations(IntellijFileContext angularModule, ModuleElementScope scope, String className) {
        return element -> {
            switch (scope) {
                case EXPORT:
                    return refactorAddExport(ReflectUtils.reduceClassName(className) , angularModule.getPsiFile(), element);
                case DECLARATIONS:
                    return refactorAddDeclarations(ReflectUtils.reduceClassName(className), angularModule.getPsiFile(), element);
                case IMPORT:
                    return refactorAddImport(ReflectUtils.reduceClassName(className), angularModule.getPsiFile(), element);
                default:
                    return refactorAddProvides(ReflectUtils.reduceClassName(className), angularModule.getPsiFile(), element);
            }
        };
    }
}
