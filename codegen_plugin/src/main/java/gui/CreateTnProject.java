package gui;

import com.google.common.base.Strings;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateTnProject {
    public JPanel rootPanel;
    public JTextField projectDir;
    private JButton btProjectDir;
    public JTextField targetDir;
    private JButton btTargetDir;


    Project project;
    Module module;

    public void setProject(Project project) {
        this.project = project;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public CreateTnProject() {
        btProjectDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
                descriptor.setTitle("Select Project Target Directory");
                descriptor.setDescription("Please choose a target directory");

                final VirtualFile vfile = FileChooser.chooseFile(descriptor, project, null);

                projectDir.setText(vfile.getPath());
            }
        });
        btTargetDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
                descriptor.setTitle("Select Build Target Directory");
                descriptor.setDescription("Please choose a target directory");

                final VirtualFile vfile = FileChooser.chooseFile(descriptor, project, null);

                targetDir.setText(vfile.getPath());
            }
        });
    }
}
