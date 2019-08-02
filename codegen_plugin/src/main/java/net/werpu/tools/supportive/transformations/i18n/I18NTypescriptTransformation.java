package net.werpu.tools.supportive.transformations.i18n;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.supportive.refactor.DummyReplacePsiElement;
import net.werpu.tools.supportive.refactor.IRefactorUnit;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.transformations.IArtifactTransformation;
import net.werpu.tools.supportive.transformations.ITransformationModel;

/**
 * A transformation which applies to  a typescript transformation
 * a typescript transformation usually is
 *
 * <p />
 * &lt;prefix&gt;.&lt;key&gt;
 * <p />
 *
 * the prefix pretty much in all cases is the controller name (angularjs only)
 * and some kind of injected key hosting service/variable
 */
@Getter
@AllArgsConstructor
public class I18NTypescriptTransformation implements IArtifactTransformation {

    private final I18NTransformationModel model;

    private final String prefix;
    private final String finalKey;
    private final String finalText;


    @Override
    public String getTnDecTransformation() {
        switch(model.getParsingType()) {
            case TEXT:
                return "{{"+ getCummulatedKey() +"}}";
            default:
                return getCummulatedKey();

        }
    }

    public String getCummulatedKey() {
        return (!Strings.isNullOrEmpty(prefix)) ? prefix+"."+finalKey : finalKey;
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

