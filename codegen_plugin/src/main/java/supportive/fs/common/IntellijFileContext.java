/*

Copyright 2017 Werner Punz

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software
is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package supportive.fs.common;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import indexes.AngularIndex;
import lombok.Getter;
import supportive.refactor.IRefactorUnit;
import supportive.reflectRefact.PsiWalkFunctions;
import supportive.utils.IntellijUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static supportive.fs.common.AngularVersion.NG;
import static supportive.fs.common.AngularVersion.TN_DEC;
import static supportive.reflectRefact.PsiWalkFunctions.walkPsiTree;

/**
 * intellij deals with two levels of files
 * a) The virtual file
 * b) The Psi File parsing level
 *
 * while this makes sense from a design point of view
 * often it is overly convoluted to juggle both levels
 * Hence we are going to introduce a context wich does most of the juggling
 */


@Getter
public class IntellijFileContext {
    Project project;
    PsiFile psiFile;
    Module module;
    Document document;
    VirtualFile virtualFile;

    public IntellijFileContext(AnActionEvent event) {
        this(event.getProject(), IntellijUtils.getFolderOrFile(event));
    }

    public IntellijFileContext(Project project) {
        this(project, project.getProjectFile());
    }

    public IntellijFileContext(Project project, PsiFile psiFile) {
        this.project = project;
        this.psiFile = psiFile;
        this.virtualFile = psiFile.getVirtualFile();
        this.document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        this.module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(virtualFile);
    }

    public IntellijFileContext(Project project, VirtualFile virtualFile) {
        this.project = project;
        this.psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        this.virtualFile = virtualFile;
        this.document = (psiFile != null) ? PsiDocumentManager.getInstance(project).getDocument(psiFile) : null;
        this.module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(virtualFile);
    }

    public String getText() {
        try {
            return new String(virtualFile.contentsToByteArray(), virtualFile.getCharset());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    public String getModuleRelativePath() {
        return virtualFile.getPath().replaceAll(module.getModuleFile().getParent().getPath(), ".");
    }

    public String calculateRelPathTo(IntellijFileContext root) {
        Path routesFilePath = Paths.get(root.getVirtualFile().isDirectory() ?
                root.getVirtualFile().getPath():
                root.getVirtualFile().getParent().getPath());
        Path componentFilePath = Paths.get(getVirtualFile().getPath());
        Path relPath = routesFilePath.relativize(componentFilePath);

        return ("./" + relPath.toString())
                .replaceAll("\\\\", "/")
                .replaceAll("" +
                        "//", "/");
    }

    public IntellijFileContext getProjectDir() {
        return new IntellijFileContext(getProject(), getProject().getBaseDir());
    }


    public List<PsiElement> findPsiElements(Function<PsiElement, Boolean> psiElementVisitor) {
        return findPsiElements(psiElementVisitor, false);
    }


    public Optional<PsiElement> findPsiElement(Function<PsiElement, Boolean> psiElementVisitor) {
        List<PsiElement> found = findPsiElements(psiElementVisitor, true);
        if(found.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(found.get(0));
        }
    }

    public void setText(String text) throws IOException {
        virtualFile.setBinaryContent(text.getBytes(virtualFile.getCharset()));
    }

    public void commit() throws IOException {
        PsiDocumentManager.getInstance(project).commitDocument(document);
    }

    public List<IntellijFileContext> getChildren(Function<VirtualFile, Boolean> fileVisitor) {
        return Arrays.stream(virtualFile.getChildren())
                .filter(vFile -> fileVisitor.apply(vFile))
                .map(vFile -> new IntellijFileContext(project, vFile))
                .collect(Collectors.toList());
    }



    public void reformat() {
        CodeStyleManager.getInstance(project).reformat(psiFile);
    }

    public void reformat(Function<PsiElement, Boolean> psiElementVisitor) {
        final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
        findPsiElements(psiElementVisitor).stream().forEach(el -> {
            codeStyleManager.reformat(el);
        });
    }

    public Optional<IntellijFileContext> getParent() {
        VirtualFile parent = this.virtualFile.getParent();

        if(parent == null) {
            return Optional.empty();
        } else {
            if(!parent.getCanonicalPath().contains(project.getBaseDir().getCanonicalPath())) {
                return Optional.empty();
            }
            return Optional.ofNullable(new IntellijFileContext(project, parent));
        }
    }

    public boolean isBelow(IntellijFileContext child) {
        return child.getFolderPath().startsWith(this.getFolderPath());
    }


    public List<IntellijFileContext> findFirstUpwards(Function<PsiFile, Boolean> psiElementVisitor) {

        if(psiFile != null && psiElementVisitor.apply(this.psiFile)) {
            return Arrays.asList(this);
        }

        List<IntellijFileContext> retVal = findInCurrentDir(psiElementVisitor);
        if(retVal.isEmpty()) {
            Optional<IntellijFileContext> parent = getParent();
            if(!parent.isPresent()) {
                return Collections.emptyList();
            }
            return parent.get().findFirstUpwards(psiElementVisitor);
        }
        return retVal;
    }

    public List<IntellijFileContext> findInCurrentDir(Function<PsiFile, Boolean> psiElementVisitor) {
        return this.getChildren(vFile -> {
                IntellijFileContext ctx = new IntellijFileContext(project, vFile);
                if(ctx.getPsiFile() != null) {
                    return psiElementVisitor.apply(ctx.getPsiFile());
                } else {
                    return false;
                }
            });
    }

    /**
     * goes an element tree upwards and finds all files/dirs triggered
     * by the visitor
     * @param psiElementVisitor an element visitor which returns true once it has found everything
     *
     * @param recurseOnceFound recurses deeper into the tree if set to true even if an element already is found
     * @return
     */
    public List<IntellijFileContext> find(Function<PsiFile, Boolean> psiElementVisitor, boolean recurseOnceFound) {
        if(psiFile != null && psiElementVisitor.apply(this.psiFile)) {
            return Arrays.asList(this);
        }
        List<IntellijFileContext> retVal = findInCurrentDir(psiElementVisitor);
        if(!recurseOnceFound && !retVal.isEmpty()) {
            return retVal;
        }
        return getChildren(virtualFile1 -> {
            return virtualFile1.isDirectory() && !virtualFile1.getName().startsWith(".");

        }).stream().flatMap(intellijFileContext -> intellijFileContext.find(psiElementVisitor, recurseOnceFound).stream())
                .collect(Collectors.toList());

    }



        public void refactorContent(List<IRefactorUnit> refactorings) throws IOException {
        if(refactorings.isEmpty()) {
            return;
        }
        //all refactorings must be of the same vFile TODO add check here
        String toSplit = refactorings.get(0).getFile().getText();
        int start = 0;
        int end = 0;
        List<String> retVal = Lists.newArrayListWithCapacity(refactorings.size() * 2);

        for (IRefactorUnit refactoring : refactorings) {
            if (refactoring.getStartOffset() > 0 && end < refactoring.getStartOffset()) {
                retVal.add(toSplit.substring(start, refactoring.getStartOffset()));
                start = refactoring.getEndOffset();
            }
            retVal.add(refactoring.getRefactoredText());
            end = refactoring.getEndOffset();
        }
        if(end < toSplit.length()) {
            retVal.add(toSplit.substring(end));
        }
        this.setText(Joiner.on("").join(retVal));
    }

    protected List<PsiElement> findPsiElements(Function<PsiElement, Boolean> psiElementVisitor, boolean firstOnly) {



        if(psiFile == null) {//not parseable
            return Collections.emptyList();
        }


        return walkPsiTree(psiFile, psiElementVisitor, firstOnly);


    }


    public boolean isPsiFile() {
        return psiFile != null;
    }

    public String getFolderPath() {
        return virtualFile.isDirectory() ? virtualFile.getPath() : virtualFile.getParent().getPath();
    }


    public IRefactorUnit refactorIn(Function<PsiFile, IRefactorUnit> refactorHandler) {
        return refactorHandler.apply(this.psiFile);
    }

    public  void copy(IntellijFileContext newDir) throws IOException {
         if(!newDir.getVirtualFile().isDirectory())    {
             throw new IOException("Target is not a dir");
         }
         if(!this.isPsiFile()) {
             throw new IOException("Source is not a dir");
         }

         PsiDirectoryFactory.getInstance(project).createDirectory(newDir.getVirtualFile()).add(this.psiFile);
    }

    /**
     * dectecs thr angular version with a package.json in its root
     * @return
     */
    public Optional<AngularVersion> getAngularVersion() {

        if(AngularIndex.isAngularVersion(this, NG)) {
            return Optional.of(NG);
        } else if(AngularIndex.isAngularVersion(this, TN_DEC)) {
            return Optional.of(TN_DEC);
        }
        return Optional.empty();

    }

    public boolean isAngularChild(AngularVersion angularVersion) {

            return AngularIndex.isBelowAngularVersion(this, angularVersion);

    }

    public boolean isAngularChild() {

        return AngularIndex.isBelowAngularVersion(this, AngularVersion.NG) ||
           AngularIndex.isBelowAngularVersion(this, AngularVersion.TN_DEC);

    }

    public Optional<IntellijFileContext> getAngularRoot() {
        Optional<IntellijFileContext> fileContext = findFirstUpwards(psiFile -> psiFile.getVirtualFile().getName().equals("package.json")).stream().findFirst();

        if(!fileContext.isPresent()) {
            return Optional.empty();
        }

        return fileContext.get().getParent();
    }




    public boolean isChildOf(IntellijFileContext ctx) {
        Path child =  Paths.get(getVirtualFile().isDirectory() ? this.getVirtualFile().getPath() : this.getVirtualFile().getParent().getPath());
        Path parent = Paths.get(ctx.getVirtualFile().isDirectory() ? ctx.getVirtualFile().getPath() : ctx.getVirtualFile().getParent().getPath());

        return child.startsWith(parent);
    }


    public Stream<PsiElementContext> queryContent(Object ... items) {
        return PsiWalkFunctions.queryContent(this.getPsiFile(), items);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof IntellijFileContext)) return false;
        IntellijFileContext that = (IntellijFileContext) o;
        Path pThis = Paths.get(this.getVirtualFile().getPath());
        Path pOther = Paths.get(that.getVirtualFile().getPath());

        return pThis.relativize(pOther).toString().isEmpty();
    }


}
