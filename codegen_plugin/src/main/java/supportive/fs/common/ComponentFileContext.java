package supportive.fs.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import supportive.fs.IntellijFileContext;
import supportive.fs.TemplateFileContext;
import supportive.fs.TypescriptFileContext;
import supportive.reflectRefact.PsiWalkFunctions;
import supportive.refactor.RefactorUnit;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static supportive.reflectRefact.PsiWalkFunctions.walkPsiTree;
import static supportive.utils.StringUtils.elVis;


//TODO handle files with html refs instead of embedded and typescript variable refs
//TODO clean this mess up

/**
 * Component file context
 * with meta info and refactoring capabilites
 * <p>
 * ATM only one component def per file is possible
 */
public class ComponentFileContext extends TypescriptFileContext {

    @Getter
    Optional<PsiElement> templateText = Optional.empty();
    Optional<RangeMarker> rangeMarker = Optional.empty();
    Optional<TemplateFileContext> templateRef = Optional.empty();

    @Getter
    private String componentClassName;

    private PsiElement componentAnnotation;

    public ComponentFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
        init();
    }

    public ComponentFileContext(AnActionEvent event) {
        super(event);
        init();
    }

    public ComponentFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
        init();
    }

    public ComponentFileContext(IntellijFileContext fileContext) {
        super(fileContext);
        init();
    }

    public ComponentFileContext(IntellijFileContext fileContext, PsiElement componentAnnotation) {
        super(fileContext);
        this.componentAnnotation = componentAnnotation;
        init();
    }


    private RangeMarker replaceText(Document doc, RangeMarker marker, String newText) {
        newText = "`" + newText + "`";
        doc.replaceString(marker.getStartOffset(), marker.getEndOffset(), newText);

        return doc.createRangeMarker(marker.getStartOffset(), marker.getStartOffset() + newText.length());
    }

    public void directUpdateTemplate(String text) {
        if (templateRef.isPresent()) {
            templateRef.get().directUpdateTemplate(text);
            return;
        }
        if (this.rangeMarker.isPresent()) {
            rangeMarker = Optional.of(replaceText(getDocument(), rangeMarker.get(), text));
        }
    }

    private void init() {
        Optional<PsiElement> template = Optional.empty();
        template = getTemplate();

        componentClassName = (componentAnnotation == null) ?
                findComponentClassName().get():
                findComponentClassName(componentAnnotation).get();

        if (template.isPresent()) {
            Optional<PsiElement> templateString = Arrays.stream(template.get().getChildren())
                    .filter(el -> PsiWalkFunctions.isTemplateString(el)).findFirst();
            if (templateString.isPresent()) {
                this.templateText = templateString;
                this.rangeMarker = Optional.of(getDocument().createRangeMarker(templateString.get().getTextRange()));
            } else {
                templateRef = getTemplateRef(template.get());
            }
        }

    }

    private Optional<PsiElement> getTemplate() {
        Optional<PsiElement> template;
        if (componentAnnotation == null) {
            template = super.findPsiElements(PsiWalkFunctions::isTemplate).stream().findFirst();
        } else {
            template = Arrays.stream(componentAnnotation.getChildren()).filter(PsiWalkFunctions::isTemplate).findFirst();
        }
        return template;
    }

    public Optional<String> getTemplateTextAsStr() {

        Optional<PsiElement> template = getTemplate();
        if (template.isPresent()) {
            Optional<PsiElement> templateString = Arrays.stream(template.get().getChildren())
                    .filter(el -> PsiWalkFunctions.isTemplateString(el)).findFirst();
            if (templateString.isPresent()) {
                this.templateText = templateString;

                return Optional.ofNullable(this.templateText.get().getText().substring(1, this.templateText.get().getText().length() - 1));
            } else {
                templateRef = getTemplateRef(template.get());
                if (templateRef.isPresent()) {
                    String templateRefText = this.templateRef.get().getTemplateTextAsStr().get();
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

            if (!psiImportString.isPresent()) {
                return Optional.empty();
            }
            String importstr = psiImportString.get().getText().trim();
            importstr = importstr.substring(1, importstr.length() - 1);
            if (!importstr.endsWith(".ts")) {
                //TODO default typescript extension info from intellij
                importstr = importstr + ".ts";
            }


            //now we have an import string lets open a file on that one
            TemplateFileContext ref = new TemplateFileContext(templateVarName, getProject(), getVirtualFile().getParent().findFileByRelativePath(importstr));
            if (!ref.getVirtualFile().exists()) {
                return Optional.empty();
            }

            return Optional.of(ref);
        }
        return Optional.empty();
    }

    public Optional<String> findComponentClassName(PsiElement componentAnnotation) {
        List<PsiElement> classDefs = findPsiElements(PsiWalkFunctions::isClass);
        Optional<String> componentClassDef = classDefs.stream()
                .filter(classDef -> componentAnnotation.getTextOffset() < classDef.getTextOffset())
                .map(el -> (String) elVis(el, "nameIdentifier", "text").get()).findFirst();

        return componentClassDef;
    }

    public Optional<String> findComponentClassName() {
        List<PsiElement> componentDefs = findPsiElements(PsiWalkFunctions::isComponent);
        List<PsiElement> classDefs = findPsiElements(PsiWalkFunctions::isClass);

        Optional<String> componentClassDef = classDefs.stream().filter(classDef -> {
            for (PsiElement componentDef : componentDefs) {
                if (componentDef.getTextOffset() < classDef.getTextOffset()) return true;
            }
            return false;
        }).map(el -> (String) elVis(el, "nameIdentifier", "text").get()).findFirst();

        return componentClassDef;
    }

    private Optional<PsiElement> findImportString(String templateVarName) {
        Optional<PsiElement> theImport = super.findPsiElements(PsiWalkFunctions::isImport).stream()
                .filter(
                        el -> Arrays.stream(el.getChildren())
                                .filter(el2 -> el2.getText().equals(templateVarName))
                                .findAny()
                                .isPresent()
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
        if (!templateText.isPresent() && !templateRef.isPresent()) {
            return;
        }
        if (templateRef.isPresent()) {
            templateRef.get().setTemplateText(newText);
            return;
        }
        super.addRefactoring(new RefactorUnit(getPsiFile(), this.templateText.get(), "`" + newText + "`"));
    }


    public static List<ComponentFileContext> getInstances(IntellijFileContext fileContext) {
        List<PsiElement> componentElements = walkPsiTree(fileContext.getPsiFile(), PsiWalkFunctions::isComponent, false);
        return componentElements.stream()
                .map(el -> new ComponentFileContext(fileContext, el))
                .collect(Collectors.toList());
    }

    @Override
    public void commit() throws IOException {
        if (templateRef.isPresent()) {
            templateRef.get().commit();
        }
        super.commit();
    }

}
