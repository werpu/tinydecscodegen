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

package net.werpu.tools.supportive.utils;

import com.intellij.util.ui.tree.TreeUtil;
import net.werpu.tools.supportive.fs.common.I18NElement;
import net.werpu.tools.supportive.fs.common.IAngularFileContext;
import net.werpu.tools.toolWindows.supportive.SwingI18NTreeNode;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExpansionMonitor {

    JTree tree;
    AtomicBoolean locked = new AtomicBoolean(false);
    Set<String> expanded = ConcurrentHashMap.newKeySet();

    public ExpansionMonitor(JTree tree) {
        this.tree = tree;
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                if(locked.get()) {
                    return;
                }
                Object lastPathComponent = event.getPath().getLastPathComponent();
                if(lastPathComponent instanceof DefaultMutableTreeNode) {
                    Object userObject = ((DefaultMutableTreeNode) lastPathComponent).getUserObject();
                    if(userObject instanceof String) {
                        expanded.add((String)userObject);
                    } else if(userObject instanceof IAngularFileContext) {
                        expanded.add(((IAngularFileContext)userObject).getVirtualFile().getPath());
                    } else if(userObject instanceof I18NElement) {
                        expanded.add(((I18NElement)userObject).getFullKey());
                    }
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                if(locked.get()) {
                    return;
                }
                Object lastPathComponent = event.getPath().getLastPathComponent();
                if(lastPathComponent instanceof DefaultMutableTreeNode) {
                    Object userObject = ((DefaultMutableTreeNode) lastPathComponent).getUserObject();
                    if(userObject instanceof String) {
                        String key = (String) userObject;
                        expanded.remove(key);
                    } else if(userObject instanceof IAngularFileContext) {
                        String key = ((IAngularFileContext) userObject).getVirtualFile().getPath();
                        expanded.remove(key);
                    } else if(userObject instanceof I18NElement) {
                        expanded.remove(((I18NElement)userObject).getFullKey());
                    }
                }
            }
        });
    }

    public void restore() {


        locked.set(true);
        TreeUtil.collapseAll(tree, 100);
        Enumeration en = ((DefaultMutableTreeNode)tree.getModel().getRoot()).breadthFirstEnumeration();

        try {
            Collections.list(en).stream().forEach(node -> {
                DefaultMutableTreeNode nodeCast = (DefaultMutableTreeNode) node;
                Object userObject = nodeCast.getUserObject();
                String key = null;
                if(userObject instanceof String) {
                    key = (String) userObject;
                } else if(userObject instanceof IAngularFileContext) {
                    key = ((IAngularFileContext)userObject).getVirtualFile().getPath();
                } else if(userObject instanceof I18NElement) {
                   key = ((I18NElement)userObject).getFullKey();
                }
                if(key != null && expanded.contains(key)) {
                    try {
                        tree.expandPath(new TreePath(nodeCast.getPath()));
                    } catch(ArrayIndexOutOfBoundsException ex) {
                        //workaround for a tree issue
                        //we simply skin the expansion in this case
                    }

                }
            });
            tree.doLayout();
        } finally {
            locked.set(false);
        }

    }
}
