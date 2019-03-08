package net.werpu.tools.supportive.transformations;

import com.intellij.openapi.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.supportive.refactor.DummyReplacePsiElement;
import net.werpu.tools.supportive.refactor.IRefactorUnit;
import net.werpu.tools.supportive.refactor.RefactorUnit;

import java.io.IOException;

@Getter
@AllArgsConstructor
public class L18NTransformation implements IArtifactTransformation {

    private final L18NTransformationModel model;

    private final String finalKey;
    private final String finalText;


    @Override
    public String getTnDecTransformation() throws IOException {
        switch(model.getParsingType()) {
            case TEXT:
                return "#{'"+finalKey+"' | translate}";
            case STRING:
                return "'"+finalKey+"' | translate";
            default:
                return finalKey;

        }
    }

    public IRefactorUnit getTnDecRefactoring() throws IOException {
        return new RefactorUnit(model.getFileContext().getPsiFile(), new DummyReplacePsiElement(model.getFrom(), model.getTo()-model.getFrom()), getTnDecTransformation());
    }

    public IRefactorUnit getNgRefactoring() throws IOException {
        return new RefactorUnit(model.getFileContext().getPsiFile(), new DummyReplacePsiElement(model.getFrom(), model.getTo()-model.getFrom()), getNgTransformation());
    }


    @Override
    public String getNgTransformation() throws IOException {
        return getTnDecTransformation();
    }

    @Override
    public ITransformationModel getTransformationModel() {
        return model;
    }
}
