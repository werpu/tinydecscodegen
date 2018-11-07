package supportive.fs.common;


import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import supportive.reflectRefact.PsiWalkFunctions;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.stream.Stream.concat;
import static supportive.reflectRefact.PsiWalkFunctions.*;
import static supportive.utils.IntellijUtils.getTsExtension;


//TODO handle files with html refs instead of embedded and typescript variable refs
//TODO clean this mess up



/**
 * Component file context
 * with meta info and refactoring capabilites
 * <p>
 * ATM only one component def per file is possible
 */
public class ComponentFileContext extends AngularResourceContext {

    @Getter
    Optional<PsiElement> templateText;
    @Getter
    Optional<RangeMarker> rangeMarker;
    @Getter
    Optional<TemplateFileContext> templateRef;

    private PsiElement componentAnnotation;

    private AssociativeArraySection params;




    public ComponentFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);

    }

    public ComponentFileContext(AnActionEvent event) {
        super(event);

    }

    public ComponentFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
        postConstruct();
    }

    public ComponentFileContext(IntellijFileContext fileContext) {
        super(fileContext);

    }

    public ComponentFileContext(IntellijFileContext fileContext, PsiElement componentAnnotation) {
        super(fileContext);
        this.componentAnnotation = componentAnnotation;

    }




    public String getDisplayName() {
        return this.getClazzName() + ((getParentModule() == null) ? "" : " <" +ComponentFileGist.getFileData(psiFile).getTagName()+ "/> [" + getParentModule().getModuleName() + "]");
    }

    @Override
    public String getResourceName() {
        return getArtifactName();
    }


    private RangeMarker replaceText(Document doc, RangeMarker marker, String newText, String quot) {
        newText = quot+ newText+ quot;
        doc.replaceString(marker.getStartOffset(), marker.getEndOffset(), newText);

        return doc.createRangeMarker(marker.getStartOffset(), marker.getStartOffset() + newText.length());
    }

    public void directUpdateTemplate(String text) {
        if (templateRef.isPresent()) {
            templateRef.get().directUpdateTemplate(text);
            return;
        }
        if (this.rangeMarker.isPresent()) {
            rangeMarker = Optional.of(replaceText(getDocument(), rangeMarker.get(), text, rangeMarker.get().getStartOffset() == 0 ? "" : "`"));
        }
    }

    public boolean inTemplate(int pos) {
        if (templateRef.isPresent()) {
            return false;//external file
        }
        if (this.rangeMarker.isPresent()) {
            RangeMarker rangeMarker = this.rangeMarker.get();
            return rangeMarker.getStartOffset() <= pos && pos < rangeMarker.getEndOffset();
        }
        return false;
    }

    @Override
    protected void postConstruct() {
        super.postConstruct();

         ComponentFileGist.init();



        if (templateText == null) {
            this.templateText = empty();
        }
        if (templateRef == null) {
            this.templateRef = empty();
        }
        if (rangeMarker == null) {
            this.rangeMarker = empty();
        }
        Optional<PsiElement> template = getTemplate();

        clazzName = ComponentFileGist.getFileData(getPsiFile()).getClassName();

        if (template.isPresent()) {
            Optional<PsiElement> templateString = Arrays.stream(template.get().getChildren())
                    .filter(el -> PsiWalkFunctions.isTemplateString(el)).findFirst();
            if (templateString.isPresent()) {
                this.templateText = templateString;
                this.rangeMarker = Optional.of(getDocument().createRangeMarker(templateString.get().getTextRange()));
            } else {
                templateRef = getTemplateRef(template.get());
                if(!templateRef.isPresent()) {
                    templateRef = getTemplateHtmlRef(template.get());
                }
            }
        }
        findParentModule();
        params = resolveParameters();

    }


    private Optional<PsiElement> getTemplate() {
        return ComponentFileGist.getTemplate(psiFile, componentAnnotation);
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
                } else {
                    templateRef = getTemplateHtmlRef(template.get());
                    if(templateRef.isPresent()) {
                        String templateRefText = this.templateRef.get().getTemplateTextAsStr().get();
                        return Optional.ofNullable(templateRefText);
                    }
                }
            }
        }
        return empty();
    }


    private Optional<TemplateFileContext> getTemplateRef(PsiElement template) {
        PsiElementContext psiElementContext = new PsiElementContext(template);
        Optional<PsiElementContext> templateRef = psiElementContext.$q(JS_REFERENCE_EXPRESSION).findFirst();


        if (templateRef.isPresent()) {
            final String templateVarName = templateRef.get().getText();
            //now lets find the imports
            Optional<PsiElement> psiImportString = findImportString(templateVarName);

            if (!psiImportString.isPresent()) {
                return empty();
            }
            String importstr = psiImportString.get().getText().trim();
            importstr = importstr.substring(1, importstr.length() - 1);
            if (!importstr.endsWith(getTsExtension())) {
                //TODO default typescript extension info from intellij
                importstr = importstr + getTsExtension();
            }


            //now we have an import string lets open a file on that one
            TemplateFileContext ref = new TemplateFileContext(templateVarName, getProject(), getVirtualFile().getParent().findFileByRelativePath(importstr));
            if (!ref.getVirtualFile().exists()) {
                return empty();
            }

            return Optional.of(ref);
        }
        return empty();
    }

    private Optional<TemplateFileContext> getTemplateHtmlRef(PsiElement template) {
        PsiElementContext psiElementContext = new PsiElementContext(template);
        Optional<PsiElementContext> templateFile = psiElementContext.$q(PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
        if (templateFile.isPresent()) {
            final String templateFileName = templateFile.get().getText();
            TemplateFileContext ref = new TemplateFileContext(templateFileName, getProject(), getVirtualFile().getParent().findFileByRelativePath(templateFileName));
            if (!ref.getVirtualFile().exists()) {
                return empty();
            }

            return Optional.of(ref);
        }
        return empty();
    }



    public Optional<String> findComponentClassName() {
        return Optional.ofNullable(ComponentFileGist.getFileData(psiFile).getClassName());
    }



    public static List<ComponentFileContext> getInstances(IntellijFileContext fileContext) {
        return fileContext.$q(COMPONENT_CLASS)
                .map(el -> new ComponentFileContext(fileContext, el.getElement()))
                .collect(Collectors.toList());

    }

    public static List<ComponentFileContext> getControllerInstances(IntellijFileContext fileContext) {
        return fileContext.$q(CONTROLLER_CLASS)
                .map(el -> new ComponentFileContext(fileContext, el.getElement()))
                .collect(Collectors.toList());
    }


    @NotNull
    private AssociativeArraySection resolveParameters() {
        return new AssociativeArraySection(project, psiFile, concat($q(COMPONENT_ARGS), $q(CONTROLLER_ARGS)).findFirst().get().getElement());
    }




    @Override
    public void commit() throws IOException {
        if (templateRef.isPresent()) {
            templateRef.get().commit();
        }
        super.commit();
    }




    public String getTagName() {
       return ComponentFileGist.getFileData(getPsiFile()).getTagName();
    }


    public Icon getIcon() {
        return AllIcons.Nodes.Artifact;
    }
}
