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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import net.werpu.tools.supportive.refactor.DummyInsertPsiElement;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;

import javax.swing.*;
import java.io.IOException;
import java.util.Optional;

import static java.util.stream.Stream.concat;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;

/**
 * A psi associative array
 */
public class AssociativeArraySection extends AngularResourceContext {
    public AssociativeArraySection(Project project, PsiFile psiFile, PsiElement psiElement) {
        super(project, psiFile, psiElement);
    }

    /**
     * all the needed operations to add a value
     * <p>
     * to a def section in ngmodule aka associative array with an array of
     * values as value {[key: string]: Function | String}
     *
     * @param sectionName
     * @param partName
     * @return
     * @throws IOException
     */
    public boolean insertUpdateDefSection(String sectionName, String partName) throws IOException {

        addArgumentsSection(Optional.of(this.getResourceRoot()));
        addOrInsertSection(sectionName);
        addInsertValue(sectionName, partName);

        return false;
    }

    /**
     * puts or replaces a vlue
     *
     * @param sectionName the name of the section
     * @param value       the value
     * @throws IOException
     */
    public void put(String sectionName, String value) throws IOException {
        //PsiElement(JS:COMMA)
        Optional<PsiElementContext> prop = get(sectionName);

        //StringBuilder elValue = new StringBuilder("\"");
        //elValue.append(value).append("\"");
        if (prop.isPresent()) {
            addRefactoring(new RefactorUnit(getPsiFile(), prop.get(), value));
            commit();
        } else {
            addOrInsertSection(sectionName);
            put(sectionName, value);
        }
    }

    public Optional<PsiElementContext> get(String sectionName) throws IOException {
        //PsiElement(JS:COMMA)
        return concat($q(JS_PROPERTY, TreeQueryEngine.NAME_EQ(sectionName), JS_ARRAY_LITERAL_EXPRESSION),
                $q(JS_PROPERTY, TreeQueryEngine.NAME_EQ(sectionName), JS_LITERAL_EXPRESSION)).findFirst();
    }

    /**
     * adds or inserts a value if a section already exists
     *
     * @param sectionName the name of the section
     * @param value       the value
     * @throws IOException
     */
    public void addInsertValue(String sectionName, String value) throws IOException {
        //PsiElement(JS:COMMA)
        Optional<PsiElementContext> propsArray = $q(JS_PROPERTY, TreeQueryEngine.NAME_EQ(sectionName), JS_ARRAY_LITERAL_EXPRESSION).findFirst();
        boolean hasElement = propsArray.get().$q(PSI_ELEMENT_JS_IDENTIFIER).findFirst().isPresent();
        int insertPos = propsArray.get().$q(PSI_ELEMENT_JS_RBRACKET).reduce((el1, el2) -> el2).get().getTextRangeOffset();
        StringBuilder insertStr = new StringBuilder();
        if (hasElement) {
            insertStr.append(", ");
        }
        insertStr.append(value);
        addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(insertPos), insertStr.toString()));
        commit();
    }

    /**
     * adds the core of the arguments section
     *
     * @param ngModuleArgs
     * @throws IOException
     */
    public void addArgumentsSection(Optional<PsiElementContext> ngModuleArgs) throws IOException {
        if (ngModuleArgs.get().$q(JS_OBJECT_LITERAL_EXPRESSION).findFirst().isPresent()) {
            return;
        }
        addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(ngModuleArgs.get().getTextRangeOffset() + 1), "{}"));
        commit();
    }

    /**
     * adds or inserts a section
     *
     * @param sectionName
     */
    public void addOrInsertSection(String sectionName) throws IOException {
        PsiElementContext argsList = this.getResourceRoot();

        if (argsList.$q(JS_OBJECT_LITERAL_EXPRESSION, JS_PROPERTY, TreeQueryEngine.NAME_EQ(sectionName)).findFirst().isPresent()) {
            return;
        }

        Optional<PsiElementContext> lastSectionEnd = argsList.
                //last
                        $q(PSI_ELEMENT_JS_RBRACKET).reduce((el1, el2) -> el2);
        if (!lastSectionEnd.isPresent()) {
            addSection(sectionName);
        } else {
            addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(lastSectionEnd.get().getTextRangeOffset() + 1), ",\n    " + sectionName + ": []"));

        }
        commit();
    }

    /**
     * adds a section
     *
     * @param sectionName
     */
    public void addSection(String sectionName) throws IOException {

        PsiElementContext argsList = this.getResourceRoot();
        int insertPos = argsList.getTextRangeOffset() + Math.round(argsList.getTextLength() / 2);
        addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(insertPos), "\n    " + sectionName + ": []\n"));
        commit();
    }

    @Override
    public String getResourceName() {
        return "";
    }

    @Override
    public Icon getIcon() {
        return AllIcons.Nodes.NewParameter;
    }
}
