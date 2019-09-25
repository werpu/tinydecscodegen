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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.werpu.tools.supportive.reflectRefact.PsiAnnotationUtils.getPositionFilter;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.PARENTS_EQ;

/**
 * a model to determine the i18n key from a
 * cursor position within an embeded template
 */
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
        HandleTagTextPattern handleTagTextPattern = new HandleTagTextPattern(cursorPos, positionFilter).invoke();
        if (handleTagTextPattern.is()) return;
        Predicate<PsiElementContext> positionFilterEmbedded = handleTagTextPattern.getPositionFilterEmbedded();
        Language language = handleTagTextPattern.getLanguage();
        IntellijFileContext parsingRoot = handleTagTextPattern.getParsingRoot();
        handleXMLAttributePattern(positionFilterEmbedded, language, parsingRoot);
        return;

    }

    private void handleXMLAttributePattern(Predicate<PsiElementContext> positionFilterEmbedded, Language language, IntellijFileContext parsingRoot) {
        Optional<PsiElementContext> toReplace;
        PsiFile ramFile;//case xml attribute and xml attribute value
        toReplace = parsingRoot.$q(PSI_XML_ATTRIBUTE_VALUE).filter(positionFilterEmbedded)
                .reduce((el1, el2) -> el2);
        if (toReplace.isPresent()) {
            Optional<PsiElementContext> xmlNameElement = toReplace.get().$q(PARENTS_EQ(PSI_XML_ATTRIBUTE), XML_ATTRIBUTE_NAME).findFirst();
            String name = xmlNameElement.orElseThrow(ElementNotResolvableException::new).getText();
            PsiElementContext foundElement = toReplace.get();
            //in case of translate we make a simple replace
            //in the other cases we have to introduce an interpolation
            //sidenote interpolations for this case are already handled by the first part
            boolean translate = name.equals("translate");
            Optional<PsiElementContext> first = foundElement.$q(XML_ATTRIBUTE_VALUE).findFirst();
            if (translate) {
                fetchKey(first);
                return;
            }
            //we cannot drill deeper psi-treewise
            //now regular expressions must do the job
            String translateText = first.get().getText();
            Pattern p = Pattern.compile("('|\")([^('|\")]+)('|\").*");
            Matcher matcher = p.matcher(translateText);
            if (matcher.find()) {
                this.key = matcher.group(2);
                fixKey();
                return;
            } else {
                key = translateText;
                fixKey();
            }
        }
        throw new ElementNotResolvableException();
    }

    private void fetchKey(Optional<PsiElementContext> toReplace) {
        key = toReplace.orElseThrow(ElementNotResolvableException::new).getUnquotedText();
        fixKey();
    }

    private void fixKey() {
        if (key.startsWith("ctrl.")) {
            key = key.substring(5);
        }
        if (key.contains(" ")) {
            throw new ElementNotResolvableException();
        }
    }

    private class HandleTagTextPattern {
        private boolean myResult;
        private int cursorPos;
        private Predicate<PsiElementContext> positionFilter;
        private Predicate<PsiElementContext> positionFilterEmbedded;
        private Language language;
        private IntellijFileContext parsingRoot;

        public HandleTagTextPattern(int cursorPos, Predicate<PsiElementContext> positionFilter) {
            this.cursorPos = cursorPos;
            this.positionFilter = positionFilter;
        }

        boolean is() {
            return myResult;
        }

        public Predicate<PsiElementContext> getPositionFilterEmbedded() {
            return positionFilterEmbedded;
        }

        public Language getLanguage() {
            return language;
        }

        public IntellijFileContext getParsingRoot() {
            return parsingRoot;
        }

        public HandleTagTextPattern invoke() {
            Optional<PsiElementContext> oCtx = fileContext.$q(STRING_TEMPLATE_EXPR)
                    .filter(positionFilter)
                    .reduce((el1, el2) -> el2);
            PsiElementContext rootElementContext = oCtx.isPresent() ? oCtx.get() : new PsiElementContext(fileContext.getPsiFile()).$q("PsiElement(HTML_DOCUMENT)").findFirst().get();


            String templateText = (oCtx.isPresent()) ? oCtx.get().getText().substring(1, Math.max(0, oCtx.get().getTextLength() - 1)) : fileContext.getText();
            int newOffset = oCtx.isPresent() ? oCtx.get().getTextRangeOffset() + 1 : rootElementContext.getTextRangeOffset();
            positionFilterEmbedded = el -> {
                int offSet = el.getTextRangeOffset();
                int offSetEnd = offSet + el.getText().length();
                return cursorPos >= (offSet + newOffset) && cursorPos <= (offSetEnd + newOffset);
            };


            language = IntellijUtils.getTnDecTemplateLanguageDef().orElse(IntellijUtils.getNgTemplateLanguageDef().orElse(IntellijUtils.getHtmlLanguage()));
            PsiFile ramFile = IntellijUtils.createRamFileFromText(fileContext.getProject(),
                    "template.html",
                    templateText,
                    //TODO make a distinction between tndec and and ng via our internal detector engine
                    language.getDialects().size() > 0 ? language.getDialects().get(0) : language);
            //determine which case of embedding we have here
            parsingRoot = new IntellijFileContext(fileContext.getProject(), ramFile);

            Optional<PsiElementContext> toReplace = parsingRoot.$q(PSI_ELEMENT_JS_STRING_LITERAL).filter(positionFilterEmbedded)
                    .reduce((el1, el2) -> el2);

            if (toReplace.isPresent()) {
                fetchKey(toReplace);
                myResult = true;
                return this;
            }
            myResult = false;
            return this;
        }
    }
}
