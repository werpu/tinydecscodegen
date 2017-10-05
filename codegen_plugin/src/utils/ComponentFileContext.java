package utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class ComponentFileContext extends TypescriptFileContext {

    Optional<PsiElement> templateText = Optional.empty();
    Optional<TemplateFileContext> templateRef = Optional.empty();

    public ComponentFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public ComponentFileContext(AnActionEvent event) {
        super(event);
    }

    public ComponentFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public ComponentFileContext(IntellijFileContext fileContext) {
        super(fileContext);
    }

    public Optional<String> getTemplateText() {

        Optional<PsiElement> template = super.findPsiElements(PsiWalkFunctions::isTemplate).stream().findFirst();
        if (template.isPresent()) {
            Optional<PsiElement> templateString = Arrays.stream(template.get().getChildren())
                    .filter(el -> PsiWalkFunctions.isTemplateString(el)).findFirst();
            if (templateString.isPresent()) {
                this.templateText = templateString;

                return Optional.ofNullable(this.templateText.get().getText().substring(1, this.templateText.get().getText().length() - 1));
            } else {
                templateRef = getTemplateRef(template.get());
                if(templateRef.isPresent()) {
                    String templateRefText = this.templateRef.get().getTemplateText().get();
                    return Optional.ofNullable(templateRefText.substring(1, templateRefText.length() - 1));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<TemplateFileContext> getTemplateRef(PsiElement template) {
        Optional<PsiElement> templateRef = Arrays.stream(template.getChildren())
                .filter(el -> PsiWalkFunctions.isTemplateRef(el))
                .findFirst();

        if (templateRef.isPresent()) {
            final String templateVarName = templateRef.get().getText();
            //now lets find the imports
            Optional<PsiElement> psiImportString = findImportString(templateVarName);

            if(!psiImportString.isPresent()) {
                return Optional.empty();
            }
            String importstr = psiImportString.get().getText().trim();
            importstr = importstr.substring(1, importstr.length() - 1);
            if(!importstr.endsWith(".ts")) {
                //TODO default typescript extension info from intellij
                importstr = importstr+".ts";
            }


            //now we have an import string lets open a file on that one
            TemplateFileContext ref = new TemplateFileContext(templateVarName, project, virtualFile.getParent().findFileByRelativePath(importstr));
            if(!ref.getVirtualFile().exists()) {
                return Optional.empty();
            }

            return Optional.of(ref);
        }
        return Optional.empty();
    }

    private Optional<PsiElement> findImportString(String templateVarName) {
        Optional<PsiElement> theImport = super.findPsiElements(PsiWalkFunctions::isImport).stream()
                .filter(
                        el -> {
                            return Arrays.stream(el.getChildren())
                                    .filter(el2 -> el2.getText().equals(templateVarName))
                                    .findAny()
                                    .isPresent();
                        }
                ).findFirst();

        return getPsiImportString(theImport);
    }

    private Optional<PsiElement> getPsiImportString(Optional<PsiElement> theImport) {
        return Arrays.asList(theImport.get().getChildren()).stream()
                .filter(el -> el.toString().equals("ES6FromClause"))
                .map(el -> el.getNode().getLastChildNode().getPsi())
                .findFirst();
    }

    private PsiElement fetchStringContentElement(Optional<PsiElement> retVal) {
        return (PsiElement) retVal.get().getNode().getFirstChildNode().getTreeNext();
    }

    public void setTemplateText(String newText) {
        if(!templateText.isPresent() && !templateRef.isPresent()) {
            return;
        }
        if(templateRef.isPresent()) {
            templateRef.get().setTemplateText(newText);
            return;
        }
        super.addRefactoring(new RefactorUnit(psiFile, this.templateText.get(), "`" + newText + "`"));
    }

    @Override
    public void commit() throws IOException {
        if(templateRef.isPresent()) {
            templateRef.get().commit();
        }
        super.commit();
    }

}
