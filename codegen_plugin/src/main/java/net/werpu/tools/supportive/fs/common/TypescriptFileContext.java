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

import com.google.common.collect.Lists;
import com.intellij.ide.scratch.ScratchFileCreationHelper;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import net.werpu.tools.supportive.refactor.DummyInsertPsiElement;
import net.werpu.tools.supportive.refactor.IRefactorUnit;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions;
import net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.*;

/**
 * special file context for typescript files
 * which has typescript refactoring capabilities
 * <p>
 * The idea is to have a context with parsing and refactoring capabilities.
 * The refactoring is done by storing a chain of refactorings
 * and once the chain is done we basically make a transactional commit
 * <p>
 * The parsing originally was done via streams but that
 * basically produced to much code which was hard to read.
 * Hence I simplified 90% of all parsing cases by introducing
 * a simple query api on top of the streams.
 * <p>
 * Note this api will be refined and improved, over time
 */
public class TypescriptFileContext extends IntellijFileContext {
    @Getter
    private List<IRefactorUnit> refactorUnits = Lists.newArrayList();

    public TypescriptFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public TypescriptFileContext(AnActionEvent event) {
        this(event.getProject(), IntellijUtils.getFolderOrFile(event));
    }

    public TypescriptFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public TypescriptFileContext(IntellijFileContext fileContext) {
        super(fileContext.getProject(), fileContext.getPsiFile());
    }

    public static TypescriptFileContext fromText(Project project, String string) {
        Language typeScript = LanguageUtil.getFileTypeLanguage(FileTypeManager.getInstance().getStdFileType("TypeScript"));
        PsiFile parsedFile = ScratchFileCreationHelper.parseHeader(project, typeScript, string);
        return new TypescriptFileContext(project, parsedFile);
    }

    protected static Optional<PsiElement> findImportString(TypescriptFileContext ctxm, String templateVarName) {
        Optional<PsiElement> theImport = ctxm.$q(ANY_TS_IMPORT).map(el -> el.getElement())
                .filter(
                        el -> Arrays.stream(el.getChildren())
                                .anyMatch(el2 -> el2.getText().contains(templateVarName) && PsiWalkFunctions.queryContent(el2, PSI_ELEMENT_JS_IDENTIFIER, TreeQueryEngine.TEXT_EQ(templateVarName)).findFirst().isPresent())
                ).findFirst();

        return getPsiImportString(theImport);
    }

    protected static Optional<PsiElement> getPsiImportString(Optional<PsiElement> theImport) {

        if (!theImport.isPresent()) {
            return theImport;
        }

        return Arrays.asList(theImport.get().getChildren()).stream()
                .filter(el -> el.toString().equals(JS_ES_6_FROM_CLAUSE))
                .map(el -> el.getNode().getLastChildNode().getPsi())
                .findFirst();
    }

    /**
     * a more refined append import, which checks for an existing import and if it exists
     * then stops the import otherwise it checks for an existing typescript variable
     * and renames it if needed
     *
     * @param plannedVariable
     * @param importPath
     * @return the final name of the import variable
     */
    public String appendImport(String plannedVariable, String importPath) {
        //case a check for the same pattern as our existing import
        return appendImport(plannedVariable, "", importPath);
    }

    /**
     * recursive append import algorith, which tries to detect
     * doubles or imports having the same import variable name but a different import
     *
     * @param plannedVariable
     * @param varPostfix
     * @param importPath
     * @return
     */
    private String appendImport(String plannedVariable, String varPostfix, String importPath) {
        String varToCheck = plannedVariable + varPostfix;
        List<PsiElementContext> importIdentifiers = getImportIdentifiers(varToCheck);
        boolean varDefined = importIdentifiers.size() > 0;
        boolean importFullyExists = importIdentifiers.stream()
                .filter(importPathMatch(importPath))
                .findFirst().isPresent();
        if (importFullyExists) {
            return plannedVariable;
        } else if (varDefined) {
            varPostfix = varPostfix.replaceAll("[^0-9]+", "");
            int currentPostFixIdx = varPostfix.isEmpty() ? 0 : Integer.parseInt(varPostfix);
            currentPostFixIdx++;
            return appendImport(plannedVariable, "_" + currentPostFixIdx, importPath);

        } else {
            Optional<PsiElementContext> lastImport = this.queryContent(ANY_TS_IMPORT).reduce((import1, import2) -> import2);
            int insertPos = 0;
            if (lastImport.isPresent()) {
                insertPos = lastImport.get().getTextRangeOffset() + lastImport.get().getTextLength();
            }

            String insert = varPostfix.isEmpty() ?
                    "\nimport {%s} from \"%s\";" :
                    "\nimport {%s as %s} from \"%s\";";

            insert = varPostfix.isEmpty() ? String.format(insert, plannedVariable, importPath) :
                    String.format(insert, plannedVariable, plannedVariable + varPostfix, importPath);

            addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(insertPos), insert));
            try {
                commit();
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }
        return plannedVariable + varPostfix;
    }

    public List<PsiElementContext> getImportsWithIdentifier(String varToCheck) {
        return getImportIdentifiers(varToCheck).stream().flatMap(item -> item.queryContent(TreeQueryEngine.PARENTS, ANY_TS_IMPORT)).collect(Collectors.toList());
    }

    public List<PsiElementContext> getImportIdentifiers(String varToCheck) {
        return this.queryContent(ANY_TS_IMPORT, JS_ES_6_IMPORT_SPECIFIER, PSI_ELEMENT_JS_IDENTIFIER, TreeQueryEngine.TEXT_EQ(varToCheck)).collect(Collectors.toList());
    }

    @NotNull
    public Predicate<PsiElementContext> importPathMatch(String importPath) {
        final String origImportPath = importPath;
        if (importPath.startsWith("./..")) {
            importPath = importPath.substring(2);
        }
        final String fImportPath = importPath;
        return importIdentifier -> importIdentifier.queryContent(TreeQueryEngine.PARENTS, ANY_TS_IMPORT, PSI_ELEMENT_JS_STRING_LITERAL, TreeQueryEngine.TEXT_EQ("'" + fImportPath + "'")).findFirst().isPresent() ||
                importIdentifier.queryContent(TreeQueryEngine.PARENTS, ANY_TS_IMPORT, PSI_ELEMENT_JS_STRING_LITERAL, TreeQueryEngine.TEXT_EQ("'" + origImportPath + "'")).findFirst().isPresent();
    }

    public void addRefactoring(RefactorUnit unit) {
        this.refactorUnits.add(unit);
    }

    protected Optional<PsiElement> findImportString(String templateVarName) {
        return findImportString(this, templateVarName);
    }

    /**
     * central commit handler to perform all refactorings on the
     * source base this context targets
     *
     * @throws IOException
     */
    @Override
    public void commit() throws IOException {

        refactorUnits = refactorUnits.stream()
                .sorted(Comparator.comparing(a -> Integer.valueOf(a.getStartOffset())))
                .collect(Collectors.toList());

        super.refactorContent(refactorUnits);
        super.commit();
        refactorUnits = Lists.newLinkedList();
    }

    /**
     * a pre commit rollback simply is a cleaning of our refactor
     * unit list, there is no staged commit which can rollback a substage for
     * the time being
     */
    public void rollback() {
        this.refactorUnits = Lists.newLinkedList();
    }

}
