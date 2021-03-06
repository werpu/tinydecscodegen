/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.supportive.fs.common;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;

import java.util.Optional;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;

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
    Optional<RangeMarker> rangeMarker = Optional.empty();
    @Getter
    private String refName;
    private Optional<PsiElement> templateText;

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
        if (this.rangeMarker.isPresent()) {
            rangeMarker = Optional.of(replaceText(document, rangeMarker.get(), text, rangeMarker.get().getStartOffset() == 0 ? "" : "`"));
        }
    }

    private RangeMarker replaceText(Document doc, RangeMarker marker, String newText, String quot) {
        newText = quot + newText + quot;
        doc.replaceString(marker.getStartOffset(), marker.getEndOffset(), newText);

        return doc.createRangeMarker(marker.getStartOffset(), marker.getStartOffset() + newText.length());
    }

    Optional<PsiElement> getPsiTemplateText() {
        Optional<PsiElementContext> elCtx = super.queryContent(TYPE_SCRIPT_VARIABLE, TreeQueryEngine.NAME_EQ(refName), STRING_TEMPLATE_EXPR).findFirst();
        if (elCtx.isPresent()) {
            return Optional.of(elCtx.get().element);
        } else {
            //fallback to literal expression for other not determinalbe strings
            elCtx = super.queryContent(TYPE_SCRIPT_VARIABLE, TreeQueryEngine.NAME_EQ(refName), JS_LITERAL_EXPRESSION, PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
            if (elCtx.isPresent()) {
                return Optional.of(elCtx.get().element);
            }
        }
        //last fallback, entire file
        return Optional.of(getPsiFile().getOriginalElement());

        /*Optional<PsiElement> el = super.findPsiElements(el2 -> el2.toString().equals("TypeScriptVariable:"+refName)).stream()
          .filter(el2 -> el2.getChildren().length > 0 &&
          el2.getChildren()[0].toString().equals("JSStringTemplateExpression"))
                .map(el2 -> el2.getChildren()[0]).findFirst();*/

    }

    Optional<RangeMarker> getInitialRangeMarker() {
        if (!templateText.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(document.createRangeMarker(templateText.get().getTextRange()));
    }

    public Optional<String> getTemplateTextAsStr() {
        if (!templateText.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(templateText.get().getText());
    }

    public void setTemplateText(String newText) {
        if (!templateText.isPresent()) {
            return;
        }
        super.addRefactoring(new RefactorUnit(psiFile, this.templateText.get(), "`" + newText + "`"));
    }

}
