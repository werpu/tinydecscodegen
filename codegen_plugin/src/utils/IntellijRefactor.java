package utils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import dtos.NgModuleJson;
import refactor.TinyRefactoringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IntellijRefactor {

    public static final String NG_MODULE = "@NgModule";

    public static List<Offset> findNgModuleOffsets(Project project, List<VirtualFile> vFile) {
        PsiFile psiJSFile = PsiManager.getInstance(project).findFile(vFile.get(0));


        List<Offset> offsets = new ArrayList<>();
        PsiRecursiveElementWalkingVisitor myElementVisitor = new PsiRecursiveElementWalkingVisitor() {

            public void visitElement(PsiElement element) {

                if (isNgModuleElement(element)) {
                    offsets.add(new Offset(element.getTextRange().getStartOffset(), element.getTextRange().getEndOffset()));
                }
                super.visitElement(element);
            }
        };

        myElementVisitor.visitFile(psiJSFile);
        return offsets;
    }

    public List<String> splitFromOffsets(String toSplit, List<Offset> offsets) {
        int start = 0;
        int end = 0;
        List<String> retVal = Lists.newArrayListWithCapacity(offsets.size() * 2);

        for (Offset offset : offsets) {
            if (end < offset.start) {
                retVal.add(toSplit.substring(start, offset.start));
            }
            retVal.add(toSplit.substring(offset.start, offset.end));
            end = offset.end;
        }
        return retVal;
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

    public List<String> appendExport(List<String> inStr, String declare) {
        return inStr.stream().map(in -> {
            if(!in.startsWith(NG_MODULE)) {
                return in;
            }
            return appendExport(in, declare);
        }).collect(Collectors.toList());
    }

    public String appendDeclare(String inStr, String declare) {
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgModuleJson moduleData = gson.fromJson(json.toString(), NgModuleJson.class);
        moduleData.appendDeclare(declare);
        return "@NgModule(" + gson.toJson(moduleData) + ")";
    }

    public String appendImport(String inStr, String declare) {
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgModuleJson moduleData = gson.fromJson(json.toString(), NgModuleJson.class);
        moduleData.appendImport(declare);
        return "@NgModule(" + gson.toJson(moduleData) + ")";
    }

    public String appendExport(String inStr, String declare) {
        String json = TinyRefactoringUtils.getJsonString(inStr).toString();
        Gson gson = new Gson();
        NgModuleJson moduleData = gson.fromJson(json.toString(), NgModuleJson.class);
        moduleData.appendExport(declare);
        return "@NgModule(" + gson.toJson(moduleData) + ")";
    }

    private static boolean isNgModuleElement(PsiElement element) {
        return element.getText().startsWith(NG_MODULE) && element.getClass().getName().equals("com.intellij.lang.typescript.psi.impl.ES6DecoratorImpl");
    }
}
