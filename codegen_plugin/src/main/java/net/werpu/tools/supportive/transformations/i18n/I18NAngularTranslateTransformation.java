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

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.supportive.refactor.DummyReplacePsiElement;
import net.werpu.tools.supportive.refactor.IRefactorUnit;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.transformations.shared.IArtifactTransformation;
import net.werpu.tools.supportive.transformations.shared.ITransformationModel;

/**
 * Transformation class
 * for a simple angular translate transformation
 * <p>
 * usually we have three patterns |translate as piped in two subpatterns
 * and a translate=".." with the key only
 */
@Getter
@AllArgsConstructor
public class I18NAngularTranslateTransformation implements IArtifactTransformation {
    private final I18NTransformationModel model;
    private final String finalKey;
    private final String finalText;

    @Override
    public String getTnDecTransformation() {
        switch (model.getParsingType()) {
            case TEXT:
            case STRING_IN_ATTRIBUTE:
                return "{{'" + finalKey + "' | translate}}";
            case STRING:
                return "'" + finalKey + "' | translate";
            default:
                return finalKey;

        }
    }

    public IRefactorUnit getTnDecRefactoring() {
        return new RefactorUnit(model.getFileContext().getPsiFile(), new DummyReplacePsiElement(model.getFrom(), model.getTo() - model.getFrom()), getTnDecTransformation());
    }

    public IRefactorUnit getNgRefactoring() {
        return new RefactorUnit(model.getFileContext().getPsiFile(), new DummyReplacePsiElement(model.getFrom(), model.getTo() - model.getFrom()), getNgTransformation());
    }

    @Override
    public String getNgTransformation() {
        return getTnDecTransformation();
    }

    @Override
    public ITransformationModel getTransformationModel() {
        return model;
    }
}
