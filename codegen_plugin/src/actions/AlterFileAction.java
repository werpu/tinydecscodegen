package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import utils.IntellijUtils;

import java.util.List;




public class AlterFileAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {

        Project project = IntellijUtils.getProject(event);
        /*Editor editor = IntellijUtils.getEditor(event);
        if (editor == null) {
            Messages.showErrorDialog(project, "There is no editor selected", "No editor selected");
            return;
        }

        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        PsiJavaFile javaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(vFile);*/

        List<VirtualFile> vFile = IntellijUtils.findFileByRelativePath(project, "webapp/typescript/app-module.ts");
        //List<Offset> offsets = IntellijRefactor.findNgModuleOffsets(project, vFile);

        System.out.println("offeset");
        // PsiFileFactory.getInstance(project).createFileFromText()
        //PsiDirectory.add()
     //  PsiFile psiJSFile = PsiManager.getInstance(project).findFile(vFile.get(0));
      // FileType ft = psiJSFile.getFileType();
       //psiJSFile.getNode(); vFile.
        //psiJSFile.getNode().getChildren(TokenSet.ANY)[6].getElementType();

        //psiJSFile.getNode().getChildren(TokenSet.ANY)[6].getElementType().toString()
        //FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, FileType.INSTANCE, GlobalSearchScope.projectScope(project))


    }

}
