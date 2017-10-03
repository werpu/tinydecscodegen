package utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
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
        final String text = psiJSFile.getText();
        if(Strings.isNullOrEmpty(text) || !text.contains(ann)) {
            return false;
        }

        PsiRecursiveElementWalkingVisitor myElementVisitor = new PsiRecursiveElementWalkingVisitor() {

            public void visitElement(PsiElement element) {
                if(hasFound.get()) {
                    return;
                }

                if (isAnnotatedElement(element, ann)) {
                    hasFound.set(true);
                    stopWalking();
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
                    return; //no need to go deeper
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

    public static void addImport(String className, PsiFile module, String relativePath, List<RefactorUnit> finalRefactorings) {
        String importStatement = "\nimport {" + className + "} from \"" + relativePath + "/" + className + "\";";
        if(module.getText().contains(importStatement)) { //skip if existing
            return;
        }
        finalRefactorings.add(generateAppendAfterImport(module, importStatement));
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
        NgModuleJson moduleData = gson.fromJson(json.toString(), NgModuleJson.class);
        moduleData.appendDeclare(declare);
        return remapToTs(gson.toJson(moduleData));
    }

    public static String appendImport(String inStr, String declare) {
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgModuleJson moduleData = gson.fromJson(json.toString(), NgModuleJson.class);
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

    private static boolean isNgModuleElement(PsiElement element) {
        return element != null && !Strings.isNullOrEmpty(element.getText()) && element.getText().startsWith(NG_MODULE) && element.getClass().getName().equals("com.intellij.lang.typescript.psi.impl.ES6DecoratorImpl");
    }

    private static boolean isAnnotatedElement(PsiElement element, String annotatedElementType) {
        return element != null && !Strings.isNullOrEmpty(element.getText()) && element.getText().startsWith(annotatedElementType) && element.getClass().getName().equals("com.intellij.lang.typescript.psi.impl.ES6DecoratorImpl");
    }
}
