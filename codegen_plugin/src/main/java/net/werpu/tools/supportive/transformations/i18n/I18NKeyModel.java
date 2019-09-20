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
 * /
 */

package net.werpu.tools.supportive.transformations.i18n;

import com.intellij.lang.Language;
import com.intellij.psi.PsiFile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.ElementNotResolvableException;
import net.werpu.tools.supportive.utils.IntellijUtils;
import net.werpu.tools.supportive.utils.SwingUtils;

import java.util.Optional;
import java.util.function.Predicate;

import static net.werpu.tools.supportive.reflectRefact.PsiAnnotationUtils.getPositionFilter;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class I18NKeyModel {
    IntellijFileContext fileContext;
    int from;
    int to;
    String key;

    public I18NKeyModel(IntellijFileContext fileContext) {
        this.fileContext = fileContext;
        parse(SwingUtils.getCurrentCursorPos(fileContext));
    }

    private void parse(int cursorPos) {
        //for now we rely on the fact that the current cursor position points to an existing key
        //or subkey
        Predicate<PsiElementContext> positionFilter = getPositionFilter(cursorPos);

        //search for an embedded string template in case of an embedded template
        //we can recycle a lot from the parsing of the i18n transformation model
        Optional<PsiElementContext> oCtx = fileContext.$q(STRING_TEMPLATE_EXPR)
                .filter(positionFilter)
                .reduce((el1, el2) -> el2);
        PsiElementContext rootElementContext = oCtx.isPresent() ? oCtx.get() : new PsiElementContext(fileContext.getPsiFile()).$q("PsiElement(HTML_DOCUMENT)").findFirst().get();


        String templateText = (oCtx.isPresent()) ? oCtx.get().getText().substring(1, Math.max(0, oCtx.get().getTextLength() - 1)) : fileContext.getText();
        int newOffset = oCtx.isPresent() ? oCtx.get().getTextRangeOffset() + 1 : rootElementContext.getTextRangeOffset();
        Predicate<PsiElementContext> positionFilterEmbedded = el -> {
            int offSet = el.getTextRangeOffset();
            int offSetEnd = offSet + el.getText().length();
            return cursorPos >= (offSet + newOffset) && cursorPos <= (offSetEnd + newOffset);
        };


        Language language = IntellijUtils.getTnDecTemplateLanguageDef().orElse(IntellijUtils.getNgTemplateLanguageDef().orElse(IntellijUtils.getHtmlLanguage()));
        PsiFile ramFile = IntellijUtils.createRamFileFromText(fileContext.getProject(),
                "template.html",
                templateText,
                //TODO make a distinction between tndec and and ng via our internal detector engine
                language.getDialects().size() > 0 ? language.getDialects().get(0) : language);
        //determine which case of embedding we have here
        IntellijFileContext parsingRoot = new IntellijFileContext(fileContext.getProject(), ramFile);

        Optional<PsiElementContext> toReplace = parsingRoot.$q(PSI_ELEMENT_JS_STRING_LITERAL).filter(positionFilterEmbedded)
                .reduce((el1, el2) -> el2);

        if (toReplace.isPresent()) {
            key = toReplace.get().getUnquotedText();
            if(key.startsWith("ctrl.")) {
                key = key.substring(5);
            }
            if(key.contains(" ")) {
                throw new ElementNotResolvableException();
            }
            return;
        }

        throw new ElementNotResolvableException();

    }
}
