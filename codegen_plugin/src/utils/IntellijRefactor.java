package utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import dtos.NgModuleJson;
import org.jetbrains.annotations.NotNull;
import refactor.TinyRefactoringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IntellijRefactor {

    public static final String NG_MODULE = "@NgModule";


    public static boolean hasAnnotatedElement(PsiFile psiJSFile, String ann) {
        List<Offset> offsets = new ArrayList<>();
        final AtomicBoolean hasFound = new AtomicBoolean(false);
        PsiRecursiveElementWalkingVisitor myElementVisitor = new PsiRecursiveElementWalkingVisitor() {

            public void visitElement(PsiElement element) {
                if(hasFound.get()) {
                    return;
                }

                if (isAnnotatedElement(element, ann)) {
                    hasFound.set(true);
                    return;
                }
                super.visitElement(element);
            }
        };

        myElementVisitor.visitFile(psiJSFile);
        return hasFound.get();
    }


    @NotNull
    public static List<PsiElement> findAnnotatedElements(Project project,  PsiFile psiFile, String ann) {

        List<PsiElement> elements = new ArrayList<>();
        PsiRecursiveElementWalkingVisitor myElementVisitor = new PsiRecursiveElementWalkingVisitor() {

            public void visitElement(PsiElement element) {

                if (isAnnotatedElement(element, ann)) {
                    elements.add(element);
                }
                super.visitElement(element);
            }
        };

        myElementVisitor.visitFile(psiFile);
        return elements;
    }

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




    public static String refactor(List<RefactorUnit> refactorings) {
        if(refactorings.isEmpty()) {
            return "";
        }
        //all refactorings must be of the same vFile TODO add check here
        String toSplit = refactorings.get(0).getFile().getText();
        int start = 0;
        int end = 0;
        List<String> retVal = Lists.newArrayListWithCapacity(refactorings.size() * 2);

        for (RefactorUnit refactoring : refactorings) {
            if (refactoring.getStartOffset() > 0 && end < refactoring.getStartOffset()) {
                retVal.add(toSplit.substring(start, refactoring.getStartOffset()));
                start = refactoring.getEndOffset();
            }
            retVal.add(refactoring.refactoredText);
            end = refactoring.getEndOffset();
        }
        if(end < toSplit.length()) {
            retVal.add(toSplit.substring(end));
        }

        return Joiner.on("").join(retVal);
    }


    public List<String> appendDeclare(List<String> inStr, String declare) {
        return inStr.stream().map(in -> {
            if(!in.startsWith(NG_MODULE)) {
                return in;
            }
            return appendDeclare(in, declare);
        }).collect(Collectors.toList());
    }

    public List<String> appendImport(List<String> inStr, String declare) {
        return inStr.stream().map(in -> {
            if(!in.startsWith(NG_MODULE)) {
                return in;
            }
            return appendImport(in, declare);
        }).collect(Collectors.toList());
    }

    public static List<String> appendExport(List<String> inStr, String declare) {
        return inStr.stream().map(in -> {
            if(!in.startsWith(NG_MODULE)) {
                return in;
            }
            return appendExport(in, declare);
        }).collect(Collectors.toList());
    }

    public static String appendDeclare(String inStr, String declare) {
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgModuleJson moduleData = gson.fromJson(json.toString(), NgModuleJson.class);
        moduleData.appendDeclare(declare);
        return gson.toJson(moduleData);
    }

    public static String appendImport(String inStr, String declare) {
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgModuleJson moduleData = gson.fromJson(json.toString(), NgModuleJson.class);
        moduleData.appendImport(declare);
        return gson.toJson(moduleData);
    }

    public static String appendExport(String inStr, String declare) {
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgModuleJson moduleData = gson.fromJson(json.toString(), NgModuleJson.class);
        moduleData.appendExport(declare);
        return gson.toJson(moduleData);
    }

    private static boolean isNgModuleElement(PsiElement element) {
        return element.getText().startsWith(NG_MODULE) && element.getClass().getName().equals("com.intellij.lang.typescript.psi.impl.ES6DecoratorImpl");
    }

    private static boolean isAnnotatedElement(PsiElement element, String annotatedElementType) {
        return element.getText().startsWith(annotatedElementType) && element.getClass().getName().equals("com.intellij.lang.typescript.psi.impl.ES6DecoratorImpl");
    }
}
