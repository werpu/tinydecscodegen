package net.werpu.tools.gui;

import com.google.common.base.Strings;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Getter
public class CreateTnProject {
    public JPanel rootPanel;
    public JTextField projectDir;
    public JTextField targetDir;
    Project project;
    Module module;
    private JButton btProjectDir;
    private JButton btTargetDir;
    private JLabel lblTitle;
    private JTextField txtProjectName;
    private JCheckBox cbCreateDir;

    public CreateTnProject() {
        btProjectDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
                descriptor.setTitle("Select Project Target Directory");
                descriptor.setDescription("Please choose a target directory");

                final VirtualFile vfile = FileChooser.chooseFile(descriptor, project, null);

                if (vfile == null) {
                    return;
                }
                projectDir.setText(Strings.nullToEmpty(vfile.getPath()));
            }
        });
        btTargetDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
                descriptor.setTitle("Select Build Target Directory");
                descriptor.setDescription("Please choose a target directory");

                final VirtualFile vfile = FileChooser.chooseFile(descriptor, project, null);

                if (vfile == null) {
                    return;
                }
                targetDir.setText(Strings.nullToEmpty(vfile.getPath()));
            }
        });
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setModule(Module module) {
        this.module = module;
    }
}
