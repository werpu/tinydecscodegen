package net.werpu.tools.supportive.transformations.i18n;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.supportive.refactor.DummyReplacePsiElement;
import net.werpu.tools.supportive.refactor.IRefactorUnit;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.transformations.IArtifactTransformation;
import net.werpu.tools.supportive.transformations.ITransformationModel;

/**
 * Transformation class
 * for a simple angular translate transformation
 *
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
        switch(model.getParsingType()) {
            case TEXT:
                return "{{'"+finalKey+"' | translate}}";
            case STRING:
                return "'"+finalKey+"' | translate";
            default:
                return finalKey;

        }
    }




    public IRefactorUnit getTnDecRefactoring() {
        return new RefactorUnit(model.getFileContext().getPsiFile(), new DummyReplacePsiElement(model.getFrom(), model.getTo()-model.getFrom()), getTnDecTransformation());
    }

    public IRefactorUnit getNgRefactoring() {
        return new RefactorUnit(model.getFileContext().getPsiFile(), new DummyReplacePsiElement(model.getFrom(), model.getTo()-model.getFrom()), getNgTransformation());
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
