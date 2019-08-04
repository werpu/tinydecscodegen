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
import lombok.AllArgsConstructor;
import net.werpu.tools.supportive.fs.common.I18NFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.refactor.DummyInsertPsiElement;
import net.werpu.tools.supportive.refactor.DummyReplacePsiElement;
import net.werpu.tools.supportive.refactor.IRefactorUnit;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.transformations.shared.IArtifactTransformation;
import net.werpu.tools.supportive.transformations.shared.ITransformationModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.JS_PROPERTY;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.PARENTS_EQ;

/**
 * Transformation on a L18N JSON declaration file
 */
@AllArgsConstructor
public class I18NTypescriptDeclFileTransformation implements IArtifactTransformation {

    public static final String QUOT = "\"";
    I18NFileContext target;
    I18NTransformationModel transformationModel;

    String neyKey;

    String newValue;

    public I18NTypescriptDeclFileTransformation(I18NFileContext target, String neyKey, String newValue) {
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
                    .reduce((item1, item2) -> item1 +  " :{" + item2).get();


            finalString = unprocessedKey+": \""+newValue+ QUOT;

            for(int cnt = 0; cnt < unprocessed.size() -1; cnt++) {
                finalString = finalString+"}";
            }

        }

        //TODO something does not work out here yet with nested keys
        //now we need to determine whether we are the only entry
        if(lastFound == null) {
            lastFound = target.getResourceRoot();
        } else {
            lastFound = lastFound.$q(PARENTS_EQ(JS_PROPERTY)).findFirst().orElse(target.getResourceRoot());
        }

        boolean isOnlyEntry = !lastFound.$q(JS_PROPERTY).findFirst().isPresent();
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
