package utils.fs;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import utils.RefactorUnit;
import utils.fs.TypescriptFileContext;

import java.util.Optional;

/*
JSFile:Dummy.ts(0,37)
  JSVarStatement(0,36)
    JSAttributeList(0,6)
      PsiElement(JS:EXPORT_KEYWORD)('export')(0,6)
    PsiWhiteSpace(' ')(6,7)
    PsiElement(JS:VAR_KEYWORD)('var')(7,10)
    PsiWhiteSpace(' ')(10,11)
    TypeScriptVariable:template(11,35)
      PsiElement(JS:IDENTIFIER)('template')(11,19)
      PsiWhiteSpace(' ')(19,20)
      PsiElement(JS:EQ)('=')(20,21)
      PsiWhiteSpace(' ')(21,22)
      JSStringTemplateExpression(22,35)
        PsiElement(JS:BACKQUOTE)('`')(22,23)
        PsiElement(JS:STRING_TEMPLATE_PART)('hello world')(23,34)
        PsiElement(JS:BACKQUOTE)('`')(34,35)
    PsiElement(JS:SEMICOLON)(';')(35,36)
  PsiWhiteSpace('\n')(36,37)

 */

/**
 * a file context pointing to a
 * template.ts file
 * which is a file which follows the structure
 * export let template = '&lt;template text&gt;';
 */
public class TemplateFileContext extends TypescriptFileContext {

    private String refName;

    private Optional<PsiElement> templateText;
    Optional<RangeMarker> rangeMarker = Optional.empty();

    public TemplateFileContext(String refName, Project project, PsiFile psiFile) {
        super(project, psiFile);
        this.refName = refName;
        templateText = getPsiTemplateText();
        rangeMarker = getInitialRangeMarker();
    }

    public TemplateFileContext(String refName, Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
        this.refName = refName;
        templateText = getPsiTemplateText();
        rangeMarker = getInitialRangeMarker();
    }


    public void directUpdateTemplate(String text) {
        if(this.rangeMarker.isPresent()) {
            rangeMarker = Optional.of(replaceText(document, rangeMarker.get(), text));
        }
    }

    private RangeMarker replaceText(Document doc, RangeMarker marker, String newText) {
        newText = "`"+newText+"`";
        doc.replaceString(marker.getStartOffset(), marker.getEndOffset(), newText);

        return doc.createRangeMarker(marker.getStartOffset(), marker.getStartOffset()+newText.length());
    }

    Optional<PsiElement> getPsiTemplateText() {
        Optional<PsiElement> el = super.findPsiElements(el2 -> el2.toString().equals("TypeScriptVariable:"+refName)).stream()
          .filter(el2 -> el2.getChildren().length > 0 &&
          el2.getChildren()[0].toString().equals("JSStringTemplateExpression"))
                .map(el2 -> el2.getChildren()[0]).findFirst();

        return el;
    }

    Optional<RangeMarker> getInitialRangeMarker() {
        if(!templateText.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(document.createRangeMarker(templateText.get().getTextRange()));
    }

    public Optional<String> getTemplateTextAsStr() {
        if(!templateText.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(templateText.get().getText());
    }

    public void setTemplateText(String newText) {
        if(!templateText.isPresent()) {
            return;
        }
        super.addRefactoring(new RefactorUnit(psiFile, this.templateText.get(), "`" + newText + "`"));
    }
}
