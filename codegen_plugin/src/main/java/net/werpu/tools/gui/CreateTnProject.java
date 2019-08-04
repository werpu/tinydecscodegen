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
