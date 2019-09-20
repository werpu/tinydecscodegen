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

package net.werpu.tools.supportive.transformations.i18n;

import com.google.common.base.Strings;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.transformations.shared.ITransformationModel;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.ElementNotResolvableException;
import net.werpu.tools.supportive.transformations.shared.modelHelpers.PARSING_TYPE;
import net.werpu.tools.supportive.utils.IntellijUtils;
import net.werpu.tools.supportive.utils.StringUtils;
import net.werpu.tools.supportive.utils.SwingUtils;

import java.util.Optional;
import java.util.function.Predicate;

import static net.werpu.tools.supportive.reflectRefact.PsiAnnotationUtils.getPositionFilter;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.PARENTS_EQ;

/**
 * a central transformation
 * for l18n which is the core
 * for our l18n tooling
 * <p>
 * The idea is to determine the possible position
 * of the refactoring and then generate a possible key
 * <p>
 * later key reassignments check if the key exist already
 * are not part of this functionality
 * they must be handled from the outside
 *
 * Note, we do not use a specific psi element here
 * because the text might not even be a single psi element
 * it can be anything
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class I18NTransformationModel implements ITransformationModel {

    IntellijFileContext fileContext;
    int from;
    int to;
    String key;
    String value;


    PARSING_TYPE parsingType;

    /**
     * constructor which determines a possible value from
     * a given editor and position
     */
    public I18NTransformationModel(IntellijFileContext ctx) {
        //here we have to split the string apart coming from
        //the current cursor pos
        fileContext = ctx;
        parse(SwingUtils.getCurrentCursorPos(ctx));
    }

    public I18NTransformationModel(IntellijFileContext ctx, int cursorPos) {
        //here we have to split the string apart coming from
        //the current cursor pos
        fileContext = ctx;
        parse(cursorPos);
    }

    private void parse(int cursorPos) {
        //theoretically we have three possible constructs
        //attr="string"  --> attr="{{'string' | translate}}"  special case translate="string" -> translate="key"

        //non attr text -> {'key' | translate}
        //non attr {'text' | translate} -> {'key' | translate}

        //first of all we need to determine whether our file is a typescript file with an embedded string in
        //our cursor position, in this case we need ot change the entire type for the parsed element
        //to html

        //in case of a simple html file we simply can proceed from there
        Predicate<PsiElementContext> positionFilter = getPositionFilter(cursorPos);

        //search for an embedded string template in case of an embedded template
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
            PsiElementContext foundElement = toReplace.get();
            Optional<PsiElementContext> parPipe = foundElement.$q(PARENTS_EQ(ANGULAR_PIPE)).findFirst();

            //angularjs fallback
            if(!parPipe.isPresent()) {
                parPipe = foundElement.$q(PARENTS_EQ(JS_EXPRESSION_STATEMENT), ANGULAR_FILTER).findFirst();
            }
            parsingType = parPipe.isPresent() ? PARSING_TYPE.STRING_WITH_TRANSLATE : PARSING_TYPE.STRING;
            applyStandardParsingValues(newOffset, foundElement);
            return;
        }


        //case xml attribute and xml attribute value
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
            parsingType = translate ? PARSING_TYPE.STRING_WITH_TRANSLATE : PARSING_TYPE.TEXT;
            if(!Strings.isNullOrEmpty(name) && !translate) {
                parsingType = PARSING_TYPE.STRING_IN_ATTRIBUTE;
            }
            applyStandardParsingValues(newOffset, foundElement.$q(XML_ATTRIBUTE_VALUE).findFirst().orElseThrow(ElementNotResolvableException::new));
            return;
        }

        //most generic case, tag content
        toReplace = parsingRoot.$q(XML_TEXT).filter(positionFilterEmbedded)
                .reduce((el1, el2) -> el2);


        if (toReplace.isPresent()) {
            PsiElementContext foundElement = toReplace.get();
            parsingType = PARSING_TYPE.TEXT;
            applyStandardParsingValues(newOffset, foundElement);
            return;
        }

        //only if we cannot resolve the possible refactoring target point we throw this
        throw new ElementNotResolvableException();
    }

    private void applyStandardParsingValues(int offset, PsiElementContext foundElement) {
        from = offset+foundElement.getTextRangeOffset();
        to = from + foundElement.getTextLength();
        value  = foundElement.getUnquotedText();
        key = StringUtils.toLowerDash(value).replaceAll("[\\s+\\.]", "_").toUpperCase();

    }

    @Override
    public Project getProject() {
        return fileContext.getProject();
    }

    public I18NTransformationModel cloneWithNewKey(String newKey) {
        return new I18NTransformationModel(fileContext, from, to, newKey, value, parsingType);
    }


}
