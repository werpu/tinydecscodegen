package net.werpu.tools.supportive.transformations;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import net.werpu.tools.supportive.fs.common.L18NFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.refactor.DummyInsertPsiElement;
import net.werpu.tools.supportive.refactor.DummyReplacePsiElement;
import net.werpu.tools.supportive.refactor.IRefactorUnit;
import net.werpu.tools.supportive.refactor.RefactorUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.JSON_PROPERTY;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.PARENTS_EQ;

/**
 * Transformation on a L18N JSON declaration file
 */
@AllArgsConstructor
public class I18NJsonDeclFileTransformation implements IArtifactTransformation{

    public static final String QUOT = "\"";
    L18NFileContext target;
    I18NTransformationModel transformationModel;

    String neyKey;

    String newValue;

    public I18NJsonDeclFileTransformation(L18NFileContext target, String neyKey, String newValue) {
        this.target = target;
        this.neyKey = neyKey;
        this.newValue = newValue;
    }

    /*
     * we determine the position of the last parseable element and then add our key value pairs accordingly
     */
    public IRefactorUnit getTnDecRefactoring() {

        //we are going to resolve the keys as is

        String [] subKeys =  neyKey.split("\\.");
        if(subKeys.length == 0) {
            throw new IllegalStateException("Keys must be defined");
        }

        ArrayList<String> processed = new ArrayList(subKeys.length);
        ArrayList<String> unprocessed = new ArrayList(subKeys.length);

        PsiElementContext lastFound = null;
        int pos = 0;
        for(String key: subKeys) {
            processed.add(key);
            if(!target.getValue(processed.stream().toArray(String[]::new)).isPresent()) {
                if(pos > 0) {
                    lastFound = target.getValue(processed.subList(0, pos).stream().toArray(String[]::new)).get();
                }
                break;
            }
            pos++;
        }

        if(pos < subKeys.length) {
            unprocessed.addAll(Arrays.asList(subKeys).subList(pos, subKeys.length));
        }
        String finalString = "";
        if(!unprocessed.isEmpty()) {
            String unprocessedKey = unprocessed.stream()
                    .map(item ->  QUOT + item + QUOT)
                    .reduce((item1, item2) -> item1 +  " :{" + item2).get();


            finalString = unprocessedKey+": \""+newValue+ QUOT;

            for(int cnt = 0; cnt < unprocessed.size() -1; cnt++) {
                finalString = finalString+"}";
            }

        }

        //now we need to determine whether we are the only entry
        if(lastFound == null) {
            lastFound = target.getResourceRoot();
        } else {
            lastFound = lastFound.$q(PARENTS_EQ(JSON_PROPERTY)).findFirst().orElse(target.getResourceRoot());
        }

        boolean isOnlyEntry = !lastFound.$q(JSON_PROPERTY).findFirst().isPresent();
        if(!isOnlyEntry && !Strings.isNullOrEmpty(finalString)) {
            finalString = ","+finalString;
        }

        //now we have everything in place the element to insert our text as child
        //and the insertion string

        //now two special cases if unprocessed.length > 0 then we insert
        //if we have all keys in place we just replace

        if(!unprocessed.isEmpty()) {
            return new RefactorUnit(target.getPsiFile(), new DummyInsertPsiElement(lastFound.getTextRangeOffset() + lastFound.getText().length() - 1), finalString);
        } else {
            PsiElementContext toReplace = target.getValue(processed.stream().toArray(String[]::new)).get();
            return new RefactorUnit(target.getPsiFile(), new DummyReplacePsiElement(toReplace.getTextRangeOffset(), toReplace.getTextLength()), QUOT +newValue+ QUOT);
        }
    }

    public IRefactorUnit getNgRefactoring() {
        return getTnDecRefactoring();
    }


    @Override
    public String getTnDecTransformation() throws IOException {
        return null;
    }

    @Override
    public String getNgTransformation() throws IOException {
        return null;
    }

    @Override
    public ITransformationModel getTransformationModel() {
        return transformationModel;
    }

}
